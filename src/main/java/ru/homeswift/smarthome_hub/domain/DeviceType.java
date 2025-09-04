package ru.homeswift.smarthome_hub.domain;

/**
 * Типы устройств, которые мы поддерживаем.
 * Enum удобно использовать в базе (хранится как строка).
 */

public enum DeviceType {
    LIGHT,              // Лампочка
    SOCKET,             // Розетка
    SENSOR_TEMPERATURE, // Датчик температуры
    SENSOR_HUMIDITY     // Датчик влажности
}
