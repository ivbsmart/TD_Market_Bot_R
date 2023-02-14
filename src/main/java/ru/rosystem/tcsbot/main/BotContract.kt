package ru.rosystem.tcsbot.main

import org.telegram.telegrambots.meta.api.objects.Update
import ru.rosystem.tcsbot.domain.PossibleAnswer

/**
 * Контракт для бота и презентера (презентер слой логики).
 *
 * Если вспомним Android, то там [BotContract.Bot] это была бы некая view (например fragment),
 * а [BotContract.Presenter] собственно презентер.
 */
internal interface BotContract {

    interface Presenter {
        /**
         * Обработать обновление в боте.
         */
        fun handleUpdate(update: Update?)
    }

    interface Bot {
        /** Отправить простое сообщение пользователю по [chatId] */
        fun sendMessage(chatId: Long, message: String)

        fun sendMenu(chatId: Long, buttons: List<PossibleAnswer>)

        /** Напечатать список кнопок выбора сотрудников */
     //   fun selectionButtonUser(chatId: Long, titleSelection: String, users: List<User>, dataPrefix: String)

        /** Напечатать список кнопок с вопросом */
        fun askQuestion(chatId: Long, titleAsk: String, answers: List<PossibleAnswer>, dataPrefix: String)
    }


}