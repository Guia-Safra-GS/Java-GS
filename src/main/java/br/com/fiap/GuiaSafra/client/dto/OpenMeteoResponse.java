package br.com.fiap.GuiaSafra.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// SOMENTE o que a regra de negocio usa da resposta do Open-Meteo.
// ignoreUnknown = true: a API devolve varios campos extras
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(
        Double latitude,
        Double longitude,
        Daily daily
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            @JsonProperty("time") List<String> time,
            @JsonProperty("temperature_2m_max") List<Double> maxTemps,
            @JsonProperty("temperature_2m_min") List<Double> minTemps,
            @JsonProperty("precipitation_sum") List<Double> rainfall
    ) {
    }
}
