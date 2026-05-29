package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.ClimateForecast;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClimateForecastResponse(
        Long id,
        LocalDate forecastDate,
        BigDecimal minTemp,
        BigDecimal maxTemp,
        BigDecimal rainfallMm,
        BigDecimal frostProb,
        String source,
        LocalDateTime createdAt
) {
    public static ClimateForecastResponse fromEntity(ClimateForecast forecast) {
        return new ClimateForecastResponse(
                forecast.getId(),
                forecast.getForecastDate(),
                forecast.getMinTemp(),
                forecast.getMaxTemp(),
                forecast.getRainfallMm(),
                forecast.getFrostProb(),
                forecast.getSource(),
                forecast.getCreatedAt()
        );
    }
}
