package ru.rosystem.tcsbot.domain.actions

import ru.rosystem.tcsbot.data.model.Category
import ru.rosystem.tcsbot.main.settings.WorkType


/** TODO БУДЕТ переделано на сессию */
/**
 * Модель для сохранения действий пользователя
 */
internal data class UserAction(
    val userId: Long,
    val title: String
)

/**
 * Модель для сохранения доп.информации по действиям пользователей.
 */
internal data class ActionData(
  //  val selectUser: User? = null,
    val selectCategory: Category? = null,
    val authorUser: String? = null,
    val lastActionButton: Long = 0,
    val selectedWorkType: WorkType? = null,
    val purposePrice: Long = 0,
    val retailPrice: Long = 0,
    val withDoc: Boolean = false
)
