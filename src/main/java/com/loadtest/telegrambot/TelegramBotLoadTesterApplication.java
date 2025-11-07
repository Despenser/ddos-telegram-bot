package com.loadtest.telegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс приложения для нагрузочного тестирования Telegram ботов
 */
@SpringBootApplication
public class TelegramBotLoadTesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotLoadTesterApplication.class, args);
    }
}

