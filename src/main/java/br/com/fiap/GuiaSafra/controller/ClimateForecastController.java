package br.com.fiap.GuiaSafra.controller;

import br.com.fiap.GuiaSafra.dto.ClimateForecastRequest;
import br.com.fiap.GuiaSafra.dto.ClimateForecastResponse;
import br.com.fiap.GuiaSafra.service.ClimateForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/previsoes")
@RequiredArgsConstructor
@Tag(name = "Previsões Climáticas", description = "Previsão do tempo derivada de satélite (Open-Meteo). Sincroniza dados da API externa, calcula risco de geada e expõe CRUD com paginação e HATEOAS")
public class ClimateForecastController {
    private final ClimateForecastService forecastService;

    private EntityModel<ClimateForecastResponse> toModel(ClimateForecastResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ClimateForecastController.class).findById(response.id())).withSelfRel(),
                linkTo(methodOn(ClimateForecastController.class).findAll(null, null, Pageable.unpaged())).withRel("previsoes"));
    }

    @GetMapping
    @Operation(summary = "Lista previsões com paginação e filtros opcionais por origem (source) e data")
    public Page<EntityModel<ClimateForecastResponse>> findAll(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        return forecastService.searchForecasts(source, date, pageable)
                .map(ClimateForecastResponse::fromEntity)
                .map(this::toModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca previsão por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Previsão encontrada"),
            @ApiResponse(responseCode = "404", description = "Previsão não encontrada")
    })
    public EntityModel<ClimateForecastResponse> findById(@PathVariable Long id) {
        return toModel(ClimateForecastResponse.fromEntity(forecastService.findForecastById(id)));
    }

    @PostMapping("/sincronizar")
    @Operation(summary = "Busca a previsão dos próximos N dias na Open-Meteo e persiste (deriva o risco de geada da temp. mínima)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Previsões sincronizadas"),
            @ApiResponse(responseCode = "502", description = "Falha ao consultar a API de clima externa")
    })
    public CollectionModel<EntityModel<ClimateForecastResponse>> sync(
            @RequestParam(defaultValue = "7") int dias
    ) {
        List<EntityModel<ClimateForecastResponse>> models = forecastService.syncForecasts(dias).stream()
                .map(ClimateForecastResponse::fromEntity)
                .map(this::toModel)
                .toList();
        return CollectionModel.of(models,
                linkTo(methodOn(ClimateForecastController.class).findAll(null, null, Pageable.unpaged())).withSelfRel());
    }

    @PostMapping
    @Operation(summary = "Cria uma previsão manualmente (uso pontual; o fluxo principal é /sincronizar)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Previsão criada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (campos obrigatórios, frostProb fora de 0–100, minTemp > maxTemp)")
    })
    public ResponseEntity<EntityModel<ClimateForecastResponse>> create(@RequestBody @Valid ClimateForecastRequest request) {
        EntityModel<ClimateForecastResponse> model =
                toModel(ClimateForecastResponse.fromEntity(forecastService.createForecast(request)));
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma previsão")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Previsão removida"),
            @ApiResponse(responseCode = "404", description = "Previsão não encontrada")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        forecastService.deleteForecast(id);
        return ResponseEntity.noContent().build();
    }
}
