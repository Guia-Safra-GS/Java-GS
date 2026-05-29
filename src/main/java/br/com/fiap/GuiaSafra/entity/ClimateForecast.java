package br.com.fiap.GuiaSafra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Table(name = "TB_MON_CLIMATE_FORECAST")
@AllArgsConstructor
@NoArgsConstructor
public class ClimateForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dia a que a previsao se refere (coluna Oracle DATE).
    private LocalDate forecastDate;

    // BigDecimal mapeia certinho para NUMBER(p,s) - valida com ddl-auto=validate.
    private BigDecimal minTemp;

    private BigDecimal maxTemp;

    private BigDecimal rainfallMm;

    // Probabilidade de geada (0 a 100) - DERIVADA da temp minima, nao vem pronta da API.
    private BigDecimal frostProb;

    // Origem do dado: 'OPEN_METEO' quando sincronizado, 'MANUAL' quando inserido a mao.
    private String source;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
