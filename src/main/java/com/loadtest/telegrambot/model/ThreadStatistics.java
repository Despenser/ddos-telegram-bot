package com.loadtest.telegrambot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Статистика выполнения по отдельному потоку
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadStatistics {
    
    /**
     * ID потока
     */
    private int threadId;
    
    /**
     * Имя потока
     */
    private String threadName;
    
    /**
     * Количество успешных запросов
     */
    private int successfulRequests;
    
    /**
     * Количество неуспешных запросов
     */
    private int failedRequests;
    
    /**
     * Среднее время ответа в миллисекундах
     */
    private double averageResponseTime;
    
    /**
     * Общее время выполнения потока в миллисекундах
     */
    private long executionTime;
}

