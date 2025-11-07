package com.loadtest.telegrambot.controller;

import com.loadtest.telegrambot.model.TestConfiguration;
import com.loadtest.telegrambot.model.TestResult;
import com.loadtest.telegrambot.service.LoadTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST контроллер для управления нагрузочными тестами
 */
@Slf4j
@RestController
@RequestMapping("/api/load-test")
@RequiredArgsConstructor
public class LoadTestController {

    private final LoadTestService loadTestService;

    /**
     * Запуск нового нагрузочного теста
     * 
     * @param config конфигурация теста
     * @return результат запуска теста
     */
    @PostMapping("/start")
    public ResponseEntity<TestResult> startTest(@Valid @RequestBody TestConfiguration config) {
        log.info("Получен запрос на запуск нагрузочного теста");
        TestResult result = loadTestService.startLoadTest(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Получение результата теста по ID
     * 
     * @param testId ID теста
     * @return результат теста
     */
    @GetMapping("/{testId}")
    public ResponseEntity<TestResult> getTestResult(@PathVariable String testId) {
        log.info("Получен запрос на получение результата теста: {}", testId);
        return loadTestService.getTestResult(testId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получение списка всех тестов
     * 
     * @return список всех тестов
     */
    @GetMapping("/all")
    public ResponseEntity<List<TestResult>> getAllTests() {
        log.info("Получен запрос на получение всех тестов");
        List<TestResult> tests = loadTestService.getAllTests();
        return ResponseEntity.ok(tests);
    }

    /**
     * Остановка выполняющегося теста
     * 
     * @param testId ID теста
     * @return статус операции
     */
    @PostMapping("/{testId}/stop")
    public ResponseEntity<Map<String, Object>> stopTest(@PathVariable String testId) {
        log.info("Получен запрос на остановку теста: {}", testId);
        boolean stopped = loadTestService.stopTest(testId);
        
        if (stopped) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Тест " + testId + " успешно остановлен"
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Тест " + testId + " не найден или уже завершен"
            ));
        }
    }

    /**
     * Проверка здоровья API
     * 
     * @return статус здоровья
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Telegram Bot Load Tester"
        ));
    }
}

