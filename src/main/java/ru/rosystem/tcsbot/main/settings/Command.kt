package ru.rosystem.tcsbot.main.settings

/**
 * Варианты команд которые можем обработать бот.
 * Подробнее смотри в [CommandMessages]
 */
internal enum class Command(val command: String) {
    START("/start"),
    FIND_CATEGORIES("/find_categories");

    companion object {

        fun commandFromString(command: String): Command? {
            return values().find { it.command == command }
        }

        const val COMMAND_ENTITY_TYPE = "bot_command"
    }
}