package ru.rosystem.tcsbot.main.settings

/**
 * Синглтон объект хранящий ответы бота на команды.
 *
 * TODO
 * В идеале если будет необходимость вынести эти строки в ресурсы (особенно если будет нужна локализация)
 */
internal object CommandMessages {
    /** Списов всех поддерживаемых комманд, необходимо при настройке бота (в боте отце) передать этот список */
    private const val allCommand = """
        start - Начать работу
        find_reasons - Выбрать причину отказа
      
    """


    const val START_MESSAGE = "Привет, я бот. Для старта введи команду /find_categories\n"

  //  const val ANSWER_INCENTIVE = "Похвалить"
  //  const val ANSWER_PENALTY = "Поругать"

    const val ASK_CATEGORY = "Выберите товарную группу"

    const val REASON_SUCCESS = "Причина успешно внесена! ✅"

    const val ASK_TAX_TYPE = "Выберите тип налогооблажения"
    const val ASK_RETAIL_PRICE ="Введите розничную цену:"
    const val ASK_POURCHASE_PRICE ="Введите закупочную цену:"
    const val ANSWER_WITH = "С документами"
    const val ANSWER_NO = "Без документов"

   /* enum class Achievements(val id: Int, val title: String) {
        INCENTIVE(0, ANSWER_INCENTIVE),
        PENALTY(1, ANSWER_PENALTY)
    } */
}