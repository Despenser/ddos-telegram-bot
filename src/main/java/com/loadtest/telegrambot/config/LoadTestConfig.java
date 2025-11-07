package com.loadtest.telegrambot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация нагрузочного тестирования по умолчанию
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "load-test.default")
public class LoadTestConfig {
    
    /**
     * Количество потоков
     */
    private int threadCount = 10;
    
    /**
     * Количество запросов на поток
     */
    private int requestsPerThread = 100;
    
    /**
     * Задержка между запросами в миллисекундах
     */
    private long delayBetweenRequests = 100;
    
    /**
     * Максимальное время выполнения в минутах
     */
    private int maxExecutionTime = 30;
}

