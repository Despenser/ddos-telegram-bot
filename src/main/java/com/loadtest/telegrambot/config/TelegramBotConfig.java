package com.loadtest.telegrambot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для Telegram бота
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotConfig {
    
    /**
     * URL Telegram бота
     */
    private String url;
    
    /**
     * Таймаут соединения в секундах
     */
    private int connectionTimeout = 10;
    
    /**
     * Таймаут чтения в секундах
     */
    private int readTimeout = 30;
}

