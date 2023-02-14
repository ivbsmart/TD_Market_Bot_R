package ru.rosystem.tcsbot.domain


/**
 * Вспомогательная модель для формирования блоков кнопок с вопросами.
 *
 * @property title - вариант ответа.
 */
internal data class PossibleAnswer(
    val id: Int,
    val title: String
)