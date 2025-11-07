package com.loadtest.telegrambot.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Конфигурация нагрузочного теста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestConfiguration {
    
    /**
     * ID чата для отправки сообщений
     */
    @NotBlank(message = "Chat ID обязателен")
    private String chatId;
    
    /**
     * Текст сообщения для отправки
     */
    @NotBlank(message = "Текст сообщения обязателен")
    private String messageText;
    
    /**
     * Количество потоков для параллельной отправки
     */
    @Min(value = 1, message = "Количество потоков должно быть >= 1")
    private int threadCount;
    
    /**
     * Количество запросов на каждый поток
     */
    @Min(value = 1, message = "Количество запросов должно быть >= 1")
    private int requestsPerThread;
    
    /**
     * Задержка между запросами в миллисекундах
     */
    @Min(value = 0, message = "Задержка должна быть >= 0")
    private long delayBetweenRequests;
    
    /**
     * URL бота (опционально, если не задан - используется из конфигурации)
     */
    private String botUrl;
    
    /**
     * Метод API для вызова (по умолчанию sendMessage)
     */
    @Builder.Default
    private String apiMethod = "sendMessage";
}

