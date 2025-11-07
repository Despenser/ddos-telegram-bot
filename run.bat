@echo off
REM Скрипт для запуска приложения на Windows

echo ================================
echo Telegram Bot Load Tester
echo ================================
echo.

REM Проверка наличия Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ОШИБКА] Java не найдена! Пожалуйста, установите Java 17 или выше.
    pause
    exit /b 1
)

echo [INFO] Проверка зависимостей...

REM Проверка наличия Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ПРЕДУПРЕЖДЕНИЕ] Maven не найден. Попытка запуска с существующим JAR...
    if exist "target\telegram-bot-load-tester-1.0.0.jar" (
        echo [INFO] Запуск приложения...
        java -jar target\telegram-bot-load-tester-1.0.0.jar
    ) else (
        echo [ОШИБКА] JAR файл не найден. Пожалуйста, установите Maven и выполните сборку.
        pause
        exit /b 1
    )
) else (
    echo [INFO] Сборка проекта...
    call mvn clean package -DskipTests
    
    if %errorlevel% equ 0 (
        echo [INFO] Сборка успешна!
        echo [INFO] Запуск приложения...
        java -jar target\telegram-bot-load-tester-1.0.0.jar
    ) else (
        echo [ОШИБКА] Ошибка при сборке проекта.
        pause
        exit /b 1
    )
)

pause

