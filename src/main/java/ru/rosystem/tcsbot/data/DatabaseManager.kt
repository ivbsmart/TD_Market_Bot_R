package ru.rosystem.tcsbot.data

import java.sql.Connection
import java.sql.DriverManager
import java.util.*

/** Класс-синглтон для обращения к бд. */
object DatabaseManager {

    private val databaseData: DatabaseData by lazy {
        val props = Properties()
        javaClass.classLoader.getResourceAsStream("database.properties").use { props.load(it) }

        DatabaseData(
            url = props.getProperty("url"),
            login = props.getProperty("login"),
            password = props.getProperty("password")
        )
    }

    /** Получить соединение к бд. */
    fun connection(): Connection = DriverManager
        .getConnection(
            databaseData.url,
            databaseData.login,
            databaseData.password
        )

    /** Вспомогательная модель для данных подключения к бд. */
    private data class DatabaseData(
        val url: String,
        val login: String,
        val password: String
    )
}