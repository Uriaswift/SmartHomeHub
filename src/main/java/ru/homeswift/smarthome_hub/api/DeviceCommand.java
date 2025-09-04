package ru.homeswift.smarthome_hub.api;

import jakarta.validation.constraints.NotNull;

/**
 * DTO для REST-запроса "включить/выключить устройство".
 * Передается в JSON:
 * { "on": true }
 */
public record DeviceCommand(@NotNull Boolean on) {}
