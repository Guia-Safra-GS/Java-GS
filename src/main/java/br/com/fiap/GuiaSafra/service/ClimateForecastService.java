package br.com.fiap.GuiaSafra.service;

import br.com.fiap.GuiaSafra.client.ClimateApiClient;
import br.com.fiap.GuiaSafra.client.dto.OpenMeteoResponse;
import br.com.fiap.GuiaSafra.dto.ClimateForecastRequest;
import br.com.fiap.GuiaSafra.entity.ClimateForecast;
import br.com.fiap.GuiaSafra.repository.ClimateForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClimateForecastService {
    private final ClimateForecastRepository forecastRepository;
    private final ClimateApiClient climateApiClient;

    public Page<ClimateForecast> searchForecasts(String source, LocalDate date, Pageable pageable) {
        return forecastRepository.search(source, date, pageable);
    }

    public ClimateForecast findForecastById(Long id) {
        return forecastRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Climate forecast with ID %d not found", id))
        );
    }

    // Busca a previsao na Open-Meteo, mapeia so o que a regra usa e persiste.
    // Reusa a previsao existente do mesmo dia (atualiza em vez de duplicar).
    @Transactional
    public List<ClimateForecast> syncForecasts(int days) {
        int safeDays = Math.max(1, Math.min(days, 16)); // Open-Meteo aceita 1..16
        OpenMeteoResponse response = climateApiClient.fetchDailyForecast(safeDays);

        if (response == null || response.daily() == null || response.daily().time() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "A API de clima retornou uma resposta vazia ou inesperada");
        }

        OpenMeteoResponse.Daily daily = response.daily();
        List<ClimateForecast> result = new ArrayList<>();

        for (int i = 0; i < daily.time().size(); i++) {
            LocalDate date = LocalDate.parse(daily.time().get(i));
            BigDecimal minTemp = scale(daily.minTemps().get(i), 1);
            BigDecimal maxTemp = scale(daily.maxTemps().get(i), 1);
            BigDecimal rainfall = scale(daily.rainfall().get(i), 2);
            BigDecimal frostProb = deriveFrostProbability(minTemp);

            ClimateForecast forecast = forecastRepository.findFirstByForecastDate(date)
                    .orElseGet(ClimateForecast::new);
            forecast.setForecastDate(date);
            forecast.setMinTemp(minTemp);
            forecast.setMaxTemp(maxTemp);
            forecast.setRainfallMm(rainfall);
            forecast.setFrostProb(frostProb);
            forecast.setSource("OPEN_METEO");

            result.add(forecastRepository.save(forecast));
        }
        return result;
    }

    public ClimateForecast createForecast(ClimateForecastRequest request) {
        // Espelha a constraint CK_FORECAST_TEMP do banco, devolvendo 400 amigavel em vez de 500.
        if (request.minTemp().compareTo(request.maxTemp()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "minTemp must be less than or equal to maxTemp");
        }
        return forecastRepository.save(request.toEntity());
    }

    public void deleteForecast(Long id) {
        if (!forecastRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Climate forecast with ID %d not found", id));
        }
        forecastRepository.deleteById(id);
    }

    // Regra de negocio: a API nao entrega risco de geada pronto; derivamos da temp minima.
    private BigDecimal deriveFrostProbability(BigDecimal minTemp) {
        if (minTemp == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        double t = minTemp.doubleValue();
        double prob;
        if (t <= 0) {
            prob = 95;        // geada praticamente certa
        } else if (t <= 3) {
            prob = 60;        // risco alto
        } else if (t <= 5) {
            prob = 25;        // risco moderado
        } else {
            prob = 0;         // sem risco relevante
        }
        return BigDecimal.valueOf(prob).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(Double value, int decimals) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP);
    }
}
