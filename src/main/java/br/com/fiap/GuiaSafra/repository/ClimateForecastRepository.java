package br.com.fiap.GuiaSafra.repository;

import br.com.fiap.GuiaSafra.entity.ClimateForecast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ClimateForecastRepository extends JpaRepository<ClimateForecast, Long> {

    // Usado no sync para decidir entre atualizar a previsao existente daquele dia ou criar uma nova.
    Optional<ClimateForecast> findFirstByForecastDate(LocalDate forecastDate);

    @Query("SELECT c FROM ClimateForecast c WHERE " +
            "(:source IS NULL OR LOWER(c.source) = LOWER(:source)) AND " +
            "(:date IS NULL OR c.forecastDate = :date)")
    Page<ClimateForecast> search(@Param("source") String source,
                                 @Param("date") LocalDate date,
                                 Pageable pageable);
}
