#!/bin/bash

# Скрипт для запуска приложения на Linux/Mac

echo "================================"
echo "Telegram Bot Load Tester"
echo "================================"
echo ""

# Проверка наличия Java
if ! command -v java &> /dev/null; then
    echo "[ОШИБКА] Java не найдена! Пожалуйста, установите Java 17 или выше."
    exit 1
fi

echo "[INFO] Java найдена:"
java -version

echo "[INFO] Проверка зависимостей..."

# Проверка наличия Maven
if ! command -v mvn &> /dev/null; then
    echo "[ПРЕДУПРЕЖДЕНИЕ] Maven не найден. Попытка запуска с существующим JAR..."
    if [ -f "target/telegram-bot-load-tester-1.0.0.jar" ]; then
        echo "[INFO] Запуск приложения..."
        java -jar target/telegram-bot-load-tester-1.0.0.jar
    else
        echo "[ОШИБКА] JAR файл не найден. Пожалуйста, установите Maven и выполните сборку."
        exit 1
    fi
else
    echo "[INFO] Сборка проекта..."
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo "[INFO] Сборка успешна!"
        echo "[INFO] Запуск приложения..."
        java -jar target/telegram-bot-load-tester-1.0.0.jar
    else
        echo "[ОШИБКА] Ошибка при сборке проекта."
        exit 1
    fi
fi

