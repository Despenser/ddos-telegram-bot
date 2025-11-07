package com.loadtest.telegrambot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Информация об ошибке
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {
    
    /**
     * Время возникновения ошибки
     */
    private LocalDateTime timestamp;
    
    /**
     * ID потока, в котором произошла ошибка
     */
    private int threadId;
    
    /**
     * Тип ошибки
     */
    private String errorType;
    
    /**
     * Сообщение об ошибке
     */
    private String errorMessage;
    
    /**
     * HTTP код ответа (если применимо)
     */
    private Integer httpStatusCode;
    
    /**
     * Номер запроса, при котором произошла ошибка
     */
    private int requestNumber;
}

