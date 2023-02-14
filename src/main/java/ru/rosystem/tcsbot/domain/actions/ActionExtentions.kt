package ru.rosystem.tcsbot.domain.actions

/** Вспомогательные расширения для сохранения действий пользователей при работе с ботом */

/** TODO БУДЕТ переделано на сессионый формат */

/** Пользователь активировал поиск сотрудников по подстроке */
 internal val Long.actionSaveAuthor: UserAction
    get() = UserAction(this, USER_ACTION_SAVE_AUTHOR)

internal val Long.actionNeedCommentAsk: UserAction
    get() = UserAction(this, USER_ACTION_ASK_NEED_COMMENT)


internal val Long.actionLastActiveButton: UserAction
    get() = UserAction(this, USER_ACTION_SAVE_LAST_ACTIVE_BUTTON)

internal val Long.actionSelectedCategory: UserAction
    get() = UserAction(this, USER_ACTION_SELECTED_CATEGORY)

internal val Long.actionWithDoc: UserAction get() = UserAction(this, "actionWithDoc")
internal val Long.actionNoDoc: UserAction get() = UserAction(this, "actionNoDoc")
