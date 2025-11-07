package com.loadtest.telegrambot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadtest.telegrambot.config.LoadTestConfig;
import com.loadtest.telegrambot.config.TelegramBotConfig;
import com.loadtest.telegrambot.model.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сервис для выполнения нагрузочного тестирования Telegram бота
 */
@Slf4j
@Service
public class LoadTestService {

    private final TelegramBotConfig botConfig;
    private final LoadTestConfig loadTestConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private final Map<String, TestResult> activeTests = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> testFutures = new ConcurrentHashMap<>();

    public LoadTestService(TelegramBotConfig botConfig, LoadTestConfig loadTestConfig) {
        this.botConfig = botConfig;
        this.loadTestConfig = loadTestConfig;
        this.objectMapper = new ObjectMapper();
        
        // Настройка HTTP клиента
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(botConfig.getConnectionTimeout(), TimeUnit.SECONDS)
                .readTimeout(botConfig.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(botConfig.getReadTimeout(), TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(200, 5, TimeUnit.MINUTES))
                .build();
    }

    /**
     * Запуск нагрузочного теста
     */
    public TestResult startLoadTest(TestConfiguration config) {
        String testId = UUID.randomUUID().toString();
        log.info("Запуск нагрузочного теста с ID: {}", testId);
        
        // Применение значений по умолчанию
        applyDefaults(config);
        
        // Создание результата теста
        TestResult result = TestResult.builder()
                .testId(testId)
                .configuration(config)
                .startTime(LocalDateTime.now())
                .status(TestResult.TestStatus.RUNNING)
                .totalRequests(config.getThreadCount() * config.getRequestsPerThread())
                .threadStatistics(new CopyOnWriteArrayList<>())
                .errors(new CopyOnWriteArrayList<>())
                .build();
        
        activeTests.put(testId, result);
        
        // Запуск теста асинхронно
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> executeLoadTest(result));
        testFutures.put(testId, future);
        executor.shutdown();
        
        return result;
    }

    /**
     * Получение результата теста по ID
     */
    public Optional<TestResult> getTestResult(String testId) {
        return Optional.ofNullable(activeTests.get(testId));
    }

    /**
     * Получение списка всех тестов
     */
    public List<TestResult> getAllTests() {
        return new ArrayList<>(activeTests.values());
    }

    /**
     * Остановка теста
     */
    public boolean stopTest(String testId) {
        Future<?> future = testFutures.get(testId);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            if (cancelled) {
                TestResult result = activeTests.get(testId);
                if (result != null) {
                    result.setStatus(TestResult.TestStatus.CANCELLED);
                    result.setEndTime(LocalDateTime.now());
                }
            }
            return cancelled;
        }
        return false;
    }

    /**
     * Применение значений по умолчанию к конфигурации
     */
    private void applyDefaults(TestConfiguration config) {
        if (config.getBotUrl() == null || config.getBotUrl().isEmpty()) {
            config.setBotUrl(botConfig.getUrl());
        }
        if (config.getThreadCount() <= 0) {
            config.setThreadCount(loadTestConfig.getThreadCount());
        }
        if (config.getRequestsPerThread() <= 0) {
            config.setRequestsPerThread(loadTestConfig.getRequestsPerThread());
        }
        if (config.getDelayBetweenRequests() < 0) {
            config.setDelayBetweenRequests(loadTestConfig.getDelayBetweenRequests());
        }
    }

    /**
     * Выполнение нагрузочного теста
     */
    private void executeLoadTest(TestResult result) {
        TestConfiguration config = result.getConfiguration();
        long startTime = System.currentTimeMillis();
        
        log.info("Начало выполнения теста {} с {} потоками и {} запросами на поток",
                result.getTestId(), config.getThreadCount(), config.getRequestsPerThread());
        
        // Создание пула потоков
        ExecutorService executorService = Executors.newFixedThreadPool(config.getThreadCount());
        
        // Счетчики для статистики
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
        AtomicLong maxResponseTime = new AtomicLong(0);
        
        List<Future<ThreadStatistics>> futures = new ArrayList<>();
        
        // Запуск потоков
        for (int i = 0; i < config.getThreadCount(); i++) {
            final int threadId = i;
            Future<ThreadStatistics> future = executorService.submit(() -> 
                executeThreadRequests(threadId, config, result, 
                    successCount, failCount, totalResponseTime, 
                    minResponseTime, maxResponseTime)
            );
            futures.add(future);
        }
        
        // Ожидание завершения всех потоков
        executorService.shutdown();
        
        try {
            boolean completed = executorService.awaitTermination(
                    loadTestConfig.getMaxExecutionTime(), TimeUnit.MINUTES);
            
            if (!completed) {
                log.warn("Тест {} превысил максимальное время выполнения", result.getTestId());
                executorService.shutdownNow();
            }
            
            // Сбор статистики по потокам
            for (Future<ThreadStatistics> future : futures) {
                try {
                    ThreadStatistics stats = future.get(1, TimeUnit.SECONDS);
                    result.getThreadStatistics().add(stats);
                } catch (Exception e) {
                    log.error("Ошибка получения статистики потока", e);
                }
            }
            
        } catch (InterruptedException e) {
            log.error("Тест {} был прерван", result.getTestId(), e);
            result.setStatus(TestResult.TestStatus.CANCELLED);
            Thread.currentThread().interrupt();
            return;
        }
        
        // Подсчет финальной статистики
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        result.setEndTime(LocalDateTime.now());
        result.setDurationMs(duration);
        result.setSuccessfulRequests(successCount.get());
        result.setFailedRequests(failCount.get());
        
        int totalRequests = successCount.get() + failCount.get();
        if (totalRequests > 0) {
            result.setAverageResponseTime((double) totalResponseTime.get() / totalRequests);
            result.setSuccessRate((double) successCount.get() / totalRequests * 100);
        }
        
        result.setMinResponseTime(minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get());
        result.setMaxResponseTime(maxResponseTime.get());
        result.setRequestsPerSecond(totalRequests / (duration / 1000.0));
        result.setStatus(TestResult.TestStatus.COMPLETED);
        
        log.info("Тест {} завершен. Успешно: {}, Неудачно: {}, RPS: {}, Среднее время: {} мс",
                result.getTestId(), successCount.get(), failCount.get(),
                String.format("%.2f", result.getRequestsPerSecond()),
                String.format("%.2f", result.getAverageResponseTime()));
    }

    /**
     * Выполнение запросов в одном потоке
     */
    private ThreadStatistics executeThreadRequests(
            int threadId,
            TestConfiguration config,
            TestResult result,
            AtomicInteger successCount,
            AtomicInteger failCount,
            AtomicLong totalResponseTime,
            AtomicLong minResponseTime,
            AtomicLong maxResponseTime) {
        
        String threadName = Thread.currentThread().getName();
        log.debug("Поток {} начал выполнение", threadName);
        
        long threadStartTime = System.currentTimeMillis();
        int threadSuccess = 0;
        int threadFail = 0;
        long threadTotalResponseTime = 0;
        
        for (int i = 0; i < config.getRequestsPerThread(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                log.debug("Поток {} был прерван", threadName);
                break;
            }
            
            try {
                RequestStats stats = sendRequest(config, i + 1, threadId);
                
                if (stats.isSuccess()) {
                    threadSuccess++;
                    successCount.incrementAndGet();
                } else {
                    threadFail++;
                    failCount.incrementAndGet();
                    
                    // Добавление информации об ошибке
                    ErrorInfo error = ErrorInfo.builder()
                            .timestamp(LocalDateTime.now())
                            .threadId(threadId)
                            .errorType("HTTP_ERROR")
                            .errorMessage(stats.getErrorMessage())
                            .httpStatusCode(stats.getStatusCode())
                            .requestNumber(i + 1)
                            .build();
                    result.getErrors().add(error);
                }
                
                // Обновление статистики времени ответа
                long responseTime = stats.getResponseTime();
                threadTotalResponseTime += responseTime;
                totalResponseTime.addAndGet(responseTime);
                
                // Обновление минимального времени
                minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
                
                // Обновление максимального времени
                maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
                
                // Задержка между запросами
                if (config.getDelayBetweenRequests() > 0 && i < config.getRequestsPerThread() - 1) {
                    Thread.sleep(config.getDelayBetweenRequests());
                }
                
            } catch (InterruptedException e) {
                log.debug("Поток {} был прерван во время задержки", threadName);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Неожиданная ошибка в потоке {}", threadName, e);
                threadFail++;
                failCount.incrementAndGet();
                
                ErrorInfo error = ErrorInfo.builder()
                        .timestamp(LocalDateTime.now())
                        .threadId(threadId)
                        .errorType(e.getClass().getSimpleName())
                        .errorMessage(e.getMessage())
                        .requestNumber(i + 1)
                        .build();
                result.getErrors().add(error);
            }
        }
        
        long threadEndTime = System.currentTimeMillis();
        
        ThreadStatistics stats = ThreadStatistics.builder()
                .threadId(threadId)
                .threadName(threadName)
                .successfulRequests(threadSuccess)
                .failedRequests(threadFail)
                .averageResponseTime(threadSuccess + threadFail > 0 ? 
                        (double) threadTotalResponseTime / (threadSuccess + threadFail) : 0)
                .executionTime(threadEndTime - threadStartTime)
                .build();
        
        log.debug("Поток {} завершил выполнение. Успешно: {}, Неудачно: {}", 
                threadName, threadSuccess, threadFail);
        
        return stats;
    }

    /**
     * Отправка одного запроса к Telegram боту
     */
    private RequestStats sendRequest(TestConfiguration config, int requestNumber, int threadId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Формирование URL
            String url = config.getBotUrl() + "/" + config.getApiMethod();
            
            // Формирование JSON тела запроса
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("chat_id", config.getChatId());
            requestBody.put("text", config.getMessageText() + " #" + requestNumber);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            // Создание HTTP запроса
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
                    .build();
            
            // Выполнение запроса
            try (Response response = httpClient.newCall(request).execute()) {
                long responseTime = System.currentTimeMillis() - startTime;
                
                boolean success = response.isSuccessful();
                String errorMessage = null;
                
                if (!success && response.body() != null) {
                    errorMessage = response.body().string();
                }
                
                return RequestStats.builder()
                        .success(success)
                        .responseTime(responseTime)
                        .statusCode(response.code())
                        .errorMessage(errorMessage)
                        .requestNumber(requestNumber)
                        .threadId(threadId)
                        .build();
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Ошибка при отправке запроса #{} в потоке {}: {}", 
                    requestNumber, threadId, e.getMessage());
            
            return RequestStats.builder()
                    .success(false)
                    .responseTime(responseTime)
                    .statusCode(0)
                    .errorMessage(e.getMessage())
                    .requestNumber(requestNumber)
                    .threadId(threadId)
                    .build();
        }
    }
}

