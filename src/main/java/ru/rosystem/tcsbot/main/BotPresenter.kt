package ru.rosystem.tcsbot.main

import com.sun.org.apache.xpath.internal.operations.Bool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import ru.rosystem.tcsbot.data.CategoryRepository
import ru.rosystem.tcsbot.data.MarketRepository
import ru.rosystem.tcsbot.data.model.Category
import ru.rosystem.tcsbot.domain.PossibleAnswer
import ru.rosystem.tcsbot.domain.actions.*
import ru.rosystem.tcsbot.main.settings.Command
import ru.rosystem.tcsbot.main.settings.CommandMessages
import ru.rosystem.tcsbot.main.settings.WorkType

/**
 * Presenter бота. Presenter это такая сущность, прослойка, отвечающая за общую логику работы.
 *
 * @param bot - наш бот, но переданный через интерфейс
 */
internal class BotPresenterImpl(private val bot: BotContract.Bot) : BotContract.Presenter {

    /**
     * Map действий пользователя. Сохраняет информацию между обращениями к боту разных пользователей по
     * chatId
     */
    private val userActionMap = mutableMapOf<UserAction, ActionData?>()

    // /** Репозиторий для получения сотрудников */
    //  private val userRepository: UserRepository = UserRepository()

    /** Репозиторий для получения вариантов категорий */
    private val categoryRepository: CategoryRepository = CategoryRepository()

    /** Репозиторий для получения записей в маркетс по id  категории */
    private val marketRepository: MarketRepository = MarketRepository()

    // /** Репозиторий для отправки событий поощрения/взыскания */
    //  private val eventRefusionRepository = EventRefusionRepository()

    override fun handleUpdate(update: Update?) {
        println("New update - ${update?.message?.chatId} - ${update?.message?.from?.userName} - ${update?.message?.text}")
        /** Если обновление есть, то :*/
        if (update != null) {
            when {
                /** Является ли обновление callback-ом (например при нажатию по кнопкам), если да, идем слушать */
                update.hasCallbackQuery() -> handleCallback(update)
                /** Иначе проверяем, а если ли вообще сообщение, если да, то: */
                update.hasMessage() -> {
                    /** Проверяем, комманда ли это (одно из сообщений [Command] */
                    if (isCommand(update.message)) handleCommand(update) else {
                        checkChoiсeWorkType(update.message.text, update.message.chatId)
                        when {
                            userActionMap[UserAction(update.message.chatId,"VALUES_ASK")] != null -> {
                                val actionData = userActionMap[UserAction(update.message.chatId,"VALUES_ASK")]!!
                                userActionMap[UserAction(update.message.chatId,"VALUES_ASK")] = null
                                val withDocTax = if (actionData.withDoc) 7L else 2L
                                val volume = update.message.text.toLong()
                                when (actionData.selectedWorkType) {
                                    WorkType.OUR_PROFIT -> ourProfit(update.message.chatId, actionData.retailPrice, actionData.purposePrice, actionData.withDoc, volume)
                                    WorkType.PURCHASE -> zeroPurchase(update.message.chatId, update.message.text, withDocTax, volume)
                                    WorkType.RETAIL -> zeroPurchase(update.message.chatId, update.message.text, withDocTax, volume)
                                }
                            }
                            userActionMap[UserAction(
                                update.message.chatId,
                                CommandMessages.ASK_POURCHASE_PRICE
                            )] != null -> {
                                val actionData = userActionMap[UserAction(
                                    update.message.chatId,
                                    CommandMessages.ASK_POURCHASE_PRICE
                                )]!!
                                val purposePrice = actionData.purposePrice
                                userActionMap[UserAction(
                                    update.message.chatId,
                                    CommandMessages.ASK_POURCHASE_PRICE
                                )] = null
                                val retailPrice = update.message.text.toLong()
                                userActionMap[UserAction(update.message.chatId,"VALUES_ASK")] = ActionData(
                                    selectedWorkType = WorkType.OUR_PROFIT,
                                    withDoc = actionData.withDoc,
                                    purposePrice = purposePrice,
                                    retailPrice = retailPrice
                                )
                                askValues(update.message.chatId)
                            }
                            userActionMap[update.message.chatId.actionWithDoc] != null -> {
                                userActionMap[update.message.chatId.actionWithDoc] = null
                                val currentWorkType = userActionMap[UserAction(
                                    update.message.chatId,
                                    "SELECTED_WORK_TYPE"
                                )]?.selectedWorkType
                                if (currentWorkType == WorkType.OUR_PROFIT
                                ) {
                                    askRetailPrice(update, withDoc = true)
                                } else {
                                    userActionMap[UserAction(update.message.chatId,"VALUES_ASK")] = ActionData(
                                        selectedWorkType = currentWorkType,
                                        withDoc = true,
                                        retailPrice = update.message.text.toLong()
                                    )
                                    askValues(update.message.chatId)
                                }

                            }
                            userActionMap[update.message.chatId.actionNoDoc] != null -> {
                                userActionMap[update.message.chatId.actionNoDoc] = null
                                val currentWorkType = userActionMap[UserAction(
                                    update.message.chatId,
                                    "SELECTED_WORK_TYPE"
                                )]?.selectedWorkType
                                if (currentWorkType == WorkType.OUR_PROFIT
                                ) {
                                    askRetailPrice(update, withDoc = false)
                                } else {
                                    userActionMap[UserAction(update.message.chatId,"VALUES_ASK")] = ActionData(
                                        selectedWorkType = currentWorkType,
                                        withDoc = false,
                                        retailPrice = update.message.text.toLong()
                                    )
                                    askValues(update.message.chatId)
                                }
                            }
                        }

                    }

                }
            }

        }
    }

    private fun askValues(chatId: Long) {
        bot.sendMessage(chatId, "Какой объем?")
    }

    private fun ourProfit(chatId: Long, retailPrice: Long, purchasePrice: Long, withDoc: Boolean, volume: Long) {
        bot.sendMessage(chatId, "Вот тут результат расчета. Volume: $volume")
    }

    private fun askRetailPrice(update: Update, withDoc: Boolean){
        userActionMap[UserAction(
            update.message.chatId,
            CommandMessages.ASK_POURCHASE_PRICE
        )] = ActionData(purposePrice = update.message.text.toLong(), withDoc = withDoc)
        bot.sendMessage(update.message.chatId, CommandMessages.ASK_RETAIL_PRICE)
    }

    private fun zeroPurchase(chatId: Long, retailPrice: String, withDocTax: Long, volume: Long) {
        val selectedCategory = userActionMap[chatId.actionSelectedCategory]?.selectCategory
        val retailPriceL: Long = retailPrice.toLong()

        CoroutineScope(Dispatchers.IO).launch {
            val marketsId = selectedCategory!!.id
            val markets = marketRepository.getMarketById(marketsId)
            val msg = with(StringBuilder()) {
                // Строка категории
                append("Расчет закупка в 0 \n")
                // строка комментария
                //   if (retailPrice.isNotEmpty()) append("Комментарий: $retailPrice\n")
                append("Розничная цена, руб: $retailPrice\n")
                // Набор найденных маркетов
                markets.forEach { market ->
                    //  val cost = market.categoryId * market.idNameMarket
                    val cost: Double =
                        retailPriceL.toDouble() - (retailPriceL.toDouble() * market.commission / 100 + retailPriceL.toDouble() * withDocTax / 100 + market.logistics)
                    //     val cost: Double = market.commission/100
                    /**    РЦ - (РЦ*Комиссию + РЦ*Налог + Логистика) */
                    append("${market.nameMarket} - $cost\n")
                }
                toString()
            }
            bot.sendMessage(chatId, msg)
        }

    }

    private fun checkChoiсeWorkType(msg: String, chatId: Long): Boolean {
        val workType = WorkType.values().find { it.title == msg }

        if (workType != null) {
            userActionMap[UserAction(chatId, "SELECTED_WORK_TYPE")] = ActionData(selectedWorkType = workType)
            sendCategories(chatId)
        }
        return workType != null
    }

    /** Обрабатываем callback-и (В нашем случае это только клики по кнопкам) */
    private fun handleCallback(update: Update) {
        val callbackQuery = update.callbackQuery

        /**
         * В коллбэке нажатия по кнопкам всегда ожидаем
         * 3 параметра указаных через : -> ТИП:время отправки:идентификатор кнопки
         */
        val params = callbackQuery.data.split(':')
        val lastActionButtonTime = userActionMap[callbackQuery.message.chatId.actionLastActiveButton] ?: return
        /**
         * если параметров меньше или время отправки кнопки
         * не совпадает с последним активным временем, то ничего не делаем
         */
        if (params.size < 3 || params[1].toLong() != lastActionButtonTime.lastActionButton) return
        val prefix = params[0]
        val data = params[2]
        println("test: ${prefix}")
        when (prefix) {
            SELECT_CATEGORY_TYPE -> handleSelectedCategoryType(callbackQuery, data)
            ASK_NEED_COMMENT -> handleSelectedTax(callbackQuery, data)
        }
    }


    private fun handleSelectedTax(callbackQuery: CallbackQuery, data: String) {
        val result = kotlin.runCatching { data.toInt() }
        if (result.isFailure) {
            println("Ошибка парсинга выбранной id кнопки добавления комментария")
            return
        }
        result.getOrNull()?.let {
            val workType =
                userActionMap[UserAction(callbackQuery.message.chatId, "SELECTED_WORK_TYPE")]!!.selectedWorkType!!
            val currentAsk = when (workType) {
                WorkType.RETAIL -> CommandMessages.ASK_POURCHASE_PRICE
                WorkType.PURCHASE -> CommandMessages.ASK_RETAIL_PRICE
                WorkType.OUR_PROFIT -> CommandMessages.ASK_POURCHASE_PRICE
            }
            when (it) {
                YES_ANSWER_ID -> {
                    userActionMap[callbackQuery.message.chatId.actionWithDoc] = ActionData()
                    bot.sendMessage(callbackQuery.message.chatId, currentAsk)
                }
                // клик без документов
                NO_ANSWER_ID -> {
                    userActionMap[callbackQuery.message.chatId.actionNoDoc] = ActionData()
                    bot.sendMessage(callbackQuery.message.chatId, currentAsk)

                }

            }
        }
    }

    /** saveReason атавизм */
    /* private fun saveReason(chatId: Long, comment: String) {
         val selectedCategory = userActionMap[chatId.actionSelectedCategory]?.selectCategory
         val author = userActionMap[chatId.actionSaveAuthor]?.authorUser ?: ""
         selectedCategory?.let {
           //  eventRefusionRepository.setEventForRefusion(it.id, Date(), comment, author)
             bot.sendMessage(
                 chatId, "${CommandMessages.REASON_SUCCESS}${
                     if (comment.isEmpty()) "" else " Комментарий: $comment"
                 }"
             )
         }
     } */

    private fun viewPriceDoc(chatId: Long, comment: String) {
        val selectedCategory = userActionMap[chatId.actionSelectedCategory]?.selectCategory
        // var markets: Market
        CoroutineScope(Dispatchers.IO).launch {
            val marketsId = selectedCategory!!.id
            val markets = marketRepository.getMarketById(marketsId)

            bot.sendMessage(
                chatId, "${"Показ id категории  " + "${selectedCategory!!.id}"}${
                    if (comment.isEmpty()) "" else " Комментарий: $comment"
                }$markets"
            )
        }


        /*  selectedCategory?.let {
              //  eventRefusionRepository.setEventForRefusion(it.id, Date(), comment, author)

              bot.sendMessage(
                  chatId, "${"Показ id категории  " + "${selectedCategory!!.id}"}${
                      if (comment.isEmpty()) "" else " Комментарий: $comment"
                  }$markets"
              )
          } */
    }

    private fun handleSelectedCategoryType(callbackQuery: CallbackQuery, data: String) {
        val result = kotlin.runCatching { data.toInt() }
        if (result.isFailure) {
            println("Ошибка парсинга выбранной id причины")
            return
        }
        result.getOrNull()?.let {
            userActionMap[callbackQuery.message.chatId.actionSaveAuthor] =
                ActionData(authorUser = "@${callbackQuery.from.userName}")

            userActionMap[callbackQuery.message.chatId.actionSelectedCategory] =
                ActionData(selectCategory = Category(it, ""))

            val lastActionTime = System.currentTimeMillis()
            userActionMap[callbackQuery.message.chatId.actionLastActiveButton] =
                ActionData(lastActionButton = lastActionTime)
// выбираем тип налогооблажения
            bot.askQuestion(
                callbackQuery.message.chatId,
                CommandMessages.ASK_TAX_TYPE,
                listOf(
                    PossibleAnswer(YES_ANSWER_ID, CommandMessages.ANSWER_WITH),
                    PossibleAnswer(NO_ANSWER_ID, CommandMessages.ANSWER_NO)
                ), "$ASK_NEED_COMMENT:$lastActionTime"
            )
        }
    }


    /** Обрабатываем команды */
    private fun handleCommand(update: Update) {
        val message = update.message
        val command = Command.commandFromString(message.entities.first().text)
        when (command) {
            /** Напечатать приветствие */
            Command.START -> sendStart(message.chatId)
            /** Отправить список причин */
            Command.FIND_CATEGORIES -> sendCategories(message.chatId)

            /** При нормальной работе сюда никогда не попадем */
            null -> bot.sendMessage(message.chatId, "Не такой команды! (${message.entities.first().text})")
        }
    }


    private fun sendCategories(chatId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val lastActionTime = System.currentTimeMillis()
            userActionMap[chatId.actionLastActiveButton] = ActionData(lastActionButton = lastActionTime)
            val categories = categoryRepository.getAllCategory()
            bot.askQuestion(
                chatId, CommandMessages.ASK_CATEGORY,
                categories.map {
                    PossibleAnswer(
                        it.id,
                        it.title
                    )
                },
                "$SELECT_CATEGORY_TYPE:$lastActionTime"
            )
        }
    }


    private fun sendStart(chatId: Long) {
        bot.sendMenu(chatId, WorkType.values().map { PossibleAnswer(it.ordinal, it.title) })
    }

    /**
     * @return true, если сообщение является одной из команд [Command]
     */
    private fun isCommand(message: Message): Boolean {
        return if (message.entities != null && message.entities.isNotEmpty()) message.entities.first().type == Command.COMMAND_ENTITY_TYPE
        else false
    }

    companion object {

        const val SELECT_CATEGORY_TYPE = "SELECT_PUNISH_TYPE"
        const val ASK_NEED_COMMENT = "ASK_NEED_COMMENT"
        const val YES_ANSWER_ID = 0
        const val NO_ANSWER_ID = 1
    }


}