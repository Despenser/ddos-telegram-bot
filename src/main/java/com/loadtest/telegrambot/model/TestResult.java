package com.loadtest.telegrambot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Результаты нагрузочного теста
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    
    /**
     * Уникальный ID теста
     */
    private String testId;
    
    /**
     * Время начала теста
     */
    private LocalDateTime startTime;
    
    /**
     * Время окончания теста
     */
    private LocalDateTime endTime;
    
    /**
     * Общая продолжительность в миллисекундах
     */
    private long durationMs;
    
    /**
     * Конфигурация теста
     */
    private TestConfiguration configuration;
    
    /**
     * Общее количество запросов
     */
    private int totalRequests;
    
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
     * Минимальное время ответа в миллисекундах
     */
    private long minResponseTime;
    
    /**
     * Максимальное время ответа в миллисекундах
     */
    private long maxResponseTime;
    
    /**
     * Запросов в секунду (RPS)
     */
    private double requestsPerSecond;
    
    /**
     * Процент успешных запросов
     */
    private double successRate;
    
    /**
     * Детальная статистика по потокам
     */
    private List<ThreadStatistics> threadStatistics;
    
    /**
     * Ошибки, возникшие во время теста
     */
    private List<ErrorInfo> errors;
    
    /**
     * Статус теста
     */
    private TestStatus status;
    
    public enum TestStatus {
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}

