package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.ClimateForecast;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

// Usado para inserir previsao MANUALMENTE (ex.: teste, correcao).
// O fluxo principal e o /sincronizar, que busca da Open-Meteo.
public record ClimateForecastRequest(
        @NotNull(message = "forecastDate is mandatory")
        LocalDate forecastDate,

        @NotNull(message = "minTemp is mandatory")
        BigDecimal minTemp,

        @NotNull(message = "maxTemp is mandatory")
        BigDecimal maxTemp,

        @NotNull(message = "rainfallMm is mandatory")
        @PositiveOrZero(message = "rainfallMm must be zero or positive")
        BigDecimal rainfallMm,

        @NotNull(message = "frostProb is mandatory")
        @DecimalMin(value = "0", message = "frostProb must be between 0 and 100")
        @DecimalMax(value = "100", message = "frostProb must be between 0 and 100")
        BigDecimal frostProb
) {
    public ClimateForecast toEntity() {
        return ClimateForecast.builder()
                .forecastDate(forecastDate)
                .minTemp(minTemp)
                .maxTemp(maxTemp)
                .rainfallMm(rainfallMm)
                .frostProb(frostProb)
                .source("MANUAL")
                .build();
    }
}
