package ru.rosystem.tcsbot.data

import ru.rosystem.tcsbot.data.model.Category


/** Репозиторий который ходит в бд для получения оттуда список возможных причин отказа*/
internal class CategoryRepository {

    suspend fun getAllCategory(): List<Category> {
        val result = mutableListOf<Category>()
        DatabaseManager.connection().run {
            val set = createStatement().executeQuery("SELECT * FROM category_product")
            while (set.next()) {
                result.add(
                    Category(
                        id = set.getInt("id_cat_product"),
                        title = set.getString("name_cat_product")
                    )
                )
            }
            close()
        }
        return result
    }
}