package ru.rosystem.tcsbot.data.model

/**
 * Модель маркета
 */
data class Market(
    val id: Long,
    val commission: Double,
    val logistics : Double,
    val acquiring: Double,
    val difference: Double,
    val difference_limit: Double,
    val margin : Double,
    val nameMarket: String
){
    companion object {
        val NO_FOUND_USER = Market(-1 ,-1.0,  -1.0, -1.0, -1.0, -1.0, -1.0,"")
    }
}

