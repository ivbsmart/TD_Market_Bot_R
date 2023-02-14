package ru.rosystem.tcsbot.main

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
//import ru.rosystem.tcsbot.data.model.User
import ru.rosystem.tcsbot.domain.PossibleAnswer
import ru.rosystem.tcsbot.main.settings.Token

/**
 * Класс бота. Именно он общается общается с API телеграм.
 * Я постарался разделить архитектурно (подробнее смотри [BotContract])
 * и получилось так что данный класс общается напрямую с API и формирует UI
 * (например отрисовывает кнопки в определенном порядке)
 */
internal class TestAndControlBot : TelegramLongPollingBot(), BotContract.Bot {

    /** Presenter, подробнее [BotContract.Presenter] */
    private val presenter: BotContract.Presenter = BotPresenterImpl(this)

    /** Передать токен бота */
    override fun getBotToken(): String = Token.token

    /** Передать адрес бота */
    override fun getBotUsername(): String = Token.botName

    /**
     * Главный метод прослушивания событий.
     * Когда бот получает сообщение то вызывается данный метод.
     * Обработка сообщения передается [BotContract.Presenter].
     *
     */
    override fun onUpdateReceived(update: Update?) {
        presenter.handleUpdate(update)
    }

    /**
     * Метод позволяющий отправить простое сообщение пользователю.
     *
     * @param chatId - идентификатор чата с пользователем которому необходимо отправить сообщение.
     */
    override fun sendMessage(chatId: Long, message: String) {
        execute(
            SendMessage
                .builder()
                .text(message)
                .chatId("$chatId")
                .build()
        )
    }

    override fun sendMenu(chatId: Long, buttons: List<PossibleAnswer>) {
        val keyboard = ReplyKeyboardMarkup().apply {
            resizeKeyboard = true
            oneTimeKeyboard = true
            val rows = buttons.chunked(3).map {
                KeyboardRow(it.map { KeyboardButton(it.title) })
            }
            keyboard = rows
        }
        execute(
            SendMessage.builder()
                .text("Выберите пункт: ")
                .chatId(chatId.toString())
                .replyMarkup(keyboard)
                .build()
        )
    }

    /**
     * Метод отправляющий пользователю список кнопок в виде клавиатуры (кучи кнопок) для выбора
     * сотрудника для поощрения/взыскания.
     *
     * @param chatId - идентификатор чата пользователя которому необходимо отправить кнопки.
     * @param titleSelection - заголовок перед кнопками (Например: Выберите пользователя)
     * @param users - пользователи из которых будет необходимо выбирать
     * @param dataPrefix - префикс для формирования callback-а прослушивания кнопок.
     *
     *
     * P.s по факту метод плохой, так как умеет рисовать только список user-ов, но так как
     * у нас простой бот я не стал мудрить и сделал так. Если в будущем будем необходимо
     * печатать кучу разных вариантов кнопок, то обобщим.
     */
   /* override fun selectionButtonUser(chatId: Long, titleSelection: String, users: List<User>, dataPrefix: String) {
        execute(
            SendMessage
                .builder()
                .chatId("$chatId")
                .text(titleSelection)
                .replyMarkup(
                    InlineKeyboardMarkup
                        .builder()
                        .keyboard(convertListUserToShowing(users, dataPrefix))
                        .build()
                )
                .build()
        )
    } */

    /**
     * Метод позволяющий задать пользователю вопрос и предложить n количество вариантов ответов в виде кнопок.
     *
     * @param chatId - идентификатор чата пользователя которому необходимо отправить кнопки.
     * @param titleAsk - вопрос перед кнопками.
     * @param answers - варианты ответа
     * @param dataPrefix - префикс информации о нажатии по кнопке.
     */
    override fun askQuestion(chatId: Long, titleAsk: String, answers: List<PossibleAnswer>, dataPrefix: String) {
        execute(
            SendMessage
                .builder()
                .chatId("$chatId")
                .text(titleAsk)
                .replyMarkup(
                    InlineKeyboardMarkup
                        .builder()
                        .keyboard(convertAskToShowing(answers, dataPrefix))
                        .build()
                )
                .build()
        )
    }

    /**
     * Формирование блока кнопок для вопроса.
     * Формирует список из n строк по 2 кнопки в строке.
     * Если нечетное количество, то последняя кнопка занимает всю ширину
     *
     * @param dataPrefix - префикс информации о нажатии по кнопке.
     */
    private fun convertAskToShowing(answers: List<PossibleAnswer>, dataPrefix: String): List<List<InlineKeyboardButton>> {
        if (answers.isEmpty()) return emptyList()
        if (answers.size < 2) return listOf(listOf(answers.first().toBtn(dataPrefix)))
        val result = mutableListOf<List<InlineKeyboardButton>>()
        for (i in answers.indices step 2) {
            result.add(
                if (i + 1 < answers.size) {
                    listOf(
                        answers[i].toBtn(dataPrefix),
                        answers[i + 1].toBtn(dataPrefix)
                    )
                } else listOf(answers[i].toBtn(dataPrefix))
            )
        }
        return result
    }

    /**
     * Аналогично как [convertAskToShowing], но для списка юзеров
     */
   /* private fun convertListUserToShowing(users: List<User>, dataPrefix: String): List<List<InlineKeyboardButton>> {
        if (users.isEmpty()) return emptyList()
        if (users.size < 2) return listOf(listOf(users.first().toBtn(dataPrefix)))
        val result = mutableListOf<List<InlineKeyboardButton>>()
        for (i in users.indices step 2) {
            result.add(
                if (i + 1 < users.size) {
                    listOf(
                        users[i].toBtn(dataPrefix),
                        users[i + 1].toBtn(dataPrefix)
                    )
                } else listOf(users[i].toBtn(dataPrefix))
            )
        }
        return result
    } */

    /**
     * Вспомогательный метод конвертирующий модель сотрудника в кнопку для отображения в Telegram.
     *
     * @param dataPrefix - префикс информации о нажатии по кнопке.
     */
 /*   private fun User.toBtn(dataPrefix: String): InlineKeyboardButton = InlineKeyboardButton
        .builder()
        .text("$firstName $lastName")
        .callbackData("$dataPrefix:$id")
        .build()
 */
    /**
     * Вспомогательный метод конвертирующий модель ответав кнопку для отображения в Telegram.
     *
     * @param dataPrefix - префикс информации о нажатии по кнопке.
     */
    private fun PossibleAnswer.toBtn(dataPrefix: String): InlineKeyboardButton = InlineKeyboardButton
        .builder()
        .text(title)
        .callbackData("$dataPrefix:$id")
        .build()
}