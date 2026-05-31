package br.com.fiap.GuiaSafra.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Payload que o sensor (ESP32) envia ao registrar uma medicao.
// readTimestamp e opcional: se vier nulo, o service usa o instante atual (leitura "agora").
public record ReadingRequest(
        @NotNull(message = "slotId is mandatory")
        @Positive(message = "slotId must be a positive value")
        Long slotId,

        LocalDateTime readTimestamp,

        @NotNull(message = "humidity is mandatory")
        @DecimalMin(value = "0", message = "humidity must be between 0 and 100")
        @DecimalMax(value = "100", message = "humidity must be between 0 and 100")
        BigDecimal humidity,

        BigDecimal temperature
) {
}
