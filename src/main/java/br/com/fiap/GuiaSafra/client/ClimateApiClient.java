package br.com.fiap.GuiaSafra.client;

import br.com.fiap.GuiaSafra.client.dto.OpenMeteoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

// Cliente HTTP para a API externa de clima (Open-Meteo), usando o RestClient nativo do Spring.
@Component
public class ClimateApiClient {

    private final RestClient restClient;
    private final double latitude;
    private final double longitude;

    public ClimateApiClient(
            @Value("${climate.api.base-url}") String baseUrl,
            @Value("${climate.api.default-latitude}") double latitude,
            @Value("${climate.api.default-longitude}") double longitude
    ) {
        this.restClient = RestClient.create(baseUrl);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Busca a previsao diaria para os proximos dias (Open-Meteo aceita 1 a 16).
    public OpenMeteoResponse fetchDailyForecast(int days) {
        try {
            return restClient.get()
                    .uri(uri -> uri
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_sum")
                            .queryParam("timezone", "auto")
                            .queryParam("forecast_days", days)
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);
        } catch (RestClientException ex) {
            // Falha de rede/HTTP na API externa vira 502 para o cliente da NOSSA API.
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao consultar a API de clima (Open-Meteo): " + ex.getMessage());
        }
    }
}
