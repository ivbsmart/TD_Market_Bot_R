package ru.rosystem.tcsbot;

import org.apache.log4j.PropertyConfigurator;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.rosystem.tcsbot.data.DatabaseManager;
import ru.rosystem.tcsbot.main.TestAndControlBot;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Точка входа в программу.
 */
public class Main {

    public static void main(String[] args) {
        /** Доп. зависимости необходимые боту */
        Properties log4jProp = new Properties();
        log4jProp.setProperty("log4j.rootLogger", "WARN");
        PropertyConfigurator.configure(log4jProp);

        /** Регистрация бота в api */
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new TestAndControlBot());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
