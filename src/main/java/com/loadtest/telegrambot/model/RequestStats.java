package com.loadtest.telegrambot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Статистика одного запроса
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStats {
    
    /**
     * Был ли запрос успешным
     */
    private boolean success;
    
    /**
     * Время выполнения запроса в миллисекундах
     */
    private long responseTime;
    
    /**
     * HTTP код ответа
     */
    private int statusCode;
    
    /**
     * Сообщение об ошибке (если была)
     */
    private String errorMessage;
    
    /**
     * Номер запроса
     */
    private int requestNumber;
    
    /**
     * ID потока
     */
    private int threadId;
}

