package ru.homeswift.smarthome_hub.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Используем record (Java 21).
 * id в запросе для создания можно не передавать (он будет null).
 */
public record DeviceDto(
        Long id,
        @NotBlank String name,
        @NotBlank String type,
        @NotBlank String topic,
        @NotNull Boolean online,
        @NotNull Boolean onState
) {}