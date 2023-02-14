package ru.rosystem.tcsbot.data

import ru.rosystem.tcsbot.data.model.Market

/** Репозиторий который ходит в бд для получения оттуда списка записей маркета по подстроке или по id категории */
internal class MarketRepository {

    /** Поиск записей в маркете по id  категории */

    suspend fun getMarketById(id: Int): List<Market> {
        val result = mutableListOf<Market>()
        DatabaseManager.connection().run {

            val set = with(prepareStatement("SELECT *\n" +
                    "FROM markets\n" +
                    "  INNER JOIN names_market \n" +
                    "    ON markets.id_name_market = names_market.id_name_market\n" +
                    "WHERE id_cat_product = ?")) {
                setInt(1, id)
                executeQuery()
            }
            while (set.next()) {
                result.add(
                    Market(
                        id = set.getLong("id"),
                        commission = set.getDouble("commission"),
                        logistics = set.getDouble("logistics"),
                        acquiring = set.getDouble("acquiring"),
                        difference = set.getDouble("difference"),
                        difference_limit = set.getDouble("difference_limit"),
                        margin = set.getDouble("margin"),
                        nameMarket = set.getString("name_market")
                    )
                )
            }
            close()
        }
        return result
    }

}