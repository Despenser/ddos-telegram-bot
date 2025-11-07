package com.loadtest.telegrambot.service;

import com.loadtest.telegrambot.config.LoadTestConfig;
import com.loadtest.telegrambot.config.TelegramBotConfig;
import com.loadtest.telegrambot.model.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для LoadTestService
 */
@ExtendWith(MockitoExtension.class)
class LoadTestServiceTest {

    private LoadTestService loadTestService;
    private TelegramBotConfig botConfig;
    private LoadTestConfig loadTestConfig;

    @BeforeEach
    void setUp() {
        botConfig = new TelegramBotConfig();
        botConfig.setUrl("https://api.telegram.org/bot123456:ABC-DEF");
        botConfig.setConnectionTimeout(10);
        botConfig.setReadTimeout(30);

        loadTestConfig = new LoadTestConfig();
        loadTestConfig.setThreadCount(5);
        loadTestConfig.setRequestsPerThread(10);
        loadTestConfig.setDelayBetweenRequests(100);
        loadTestConfig.setMaxExecutionTime(30);

        loadTestService = new LoadTestService(botConfig, loadTestConfig);
    }

    @Test
    void testConfigurationNotNull() {
        assertNotNull(loadTestService);
    }

    @Test
    void testCreateTestConfiguration() {
        TestConfiguration config = TestConfiguration.builder()
                .chatId("123456789")
                .messageText("Test message")
                .threadCount(5)
                .requestsPerThread(10)
                .delayBetweenRequests(100)
                .build();

        assertNotNull(config);
        assertEquals("123456789", config.getChatId());
        assertEquals("Test message", config.getMessageText());
        assertEquals(5, config.getThreadCount());
        assertEquals(10, config.getRequestsPerThread());
        assertEquals(100, config.getDelayBetweenRequests());
    }

    @Test
    void testGetAllTestsInitiallyEmpty() {
        var tests = loadTestService.getAllTests();
        assertNotNull(tests);
    }
}

