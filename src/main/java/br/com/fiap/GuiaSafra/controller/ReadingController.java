package br.com.fiap.GuiaSafra.controller;

import br.com.fiap.GuiaSafra.dto.ReadingRequest;
import br.com.fiap.GuiaSafra.dto.ReadingResponse;
import br.com.fiap.GuiaSafra.service.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/leituras")
@RequiredArgsConstructor
@Tag(name = "Leituras", description = "Histórico de medições dos sensores (ESP32). Chave composta (slot + instante), valida o slot lido do domínio C#, com paginação e HATEOAS")
public class ReadingController {
    private final ReadingService readingService;

    private EntityModel<ReadingResponse> toModel(ReadingResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(ReadingController.class).findOne(response.slotId(), response.readTimestamp())).withSelfRel(),
                linkTo(methodOn(ReadingController.class).findAll(null, Pageable.unpaged())).withRel("leituras"),
                linkTo(methodOn(ReadingController.class).findAll(response.slotId(), Pageable.unpaged())).withRel("leituras-do-slot"));
    }

    @GetMapping
    @Operation(summary = "Lista leituras com paginação e filtro opcional por slotId")
    public Page<EntityModel<ReadingResponse>> findAll(
            @RequestParam(required = false) Long slotId,
            Pageable pageable
    ) {
        return readingService.searchReadings(slotId, pageable)
                .map(ReadingResponse::fromEntity)
                .map(this::toModel);
    }

    @GetMapping("/{slotId}/{readTimestamp}")
    @Operation(summary = "Busca uma leitura pela chave composta (slot + instante ISO, ex.: 2026-05-25T12:00:00)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leitura encontrada"),
            @ApiResponse(responseCode = "404", description = "Leitura não encontrada")
    })
    public EntityModel<ReadingResponse> findOne(
            @PathVariable Long slotId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime readTimestamp
    ) {
        return toModel(ReadingResponse.fromEntity(readingService.findReading(slotId, readTimestamp)));
    }

    @PostMapping
    @Operation(summary = "Registra uma nova leitura de sensor (se readTimestamp for omitido, usa o instante atual)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Leitura registrada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (humidity fora de 0–100, slotId obrigatório)"),
            @ApiResponse(responseCode = "404", description = "Slot informado não existe"),
            @ApiResponse(responseCode = "409", description = "Já existe leitura desse slot nesse instante")
    })
    public ResponseEntity<EntityModel<ReadingResponse>> create(@RequestBody @Valid ReadingRequest request) {
        EntityModel<ReadingResponse> model =
                toModel(ReadingResponse.fromEntity(readingService.createReading(request)));
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @DeleteMapping("/{slotId}/{readTimestamp}")
    @Operation(summary = "Remove uma leitura pela chave composta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Leitura removida"),
            @ApiResponse(responseCode = "404", description = "Leitura não encontrada")
    })
    public ResponseEntity<Void> delete(
            @PathVariable Long slotId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime readTimestamp
    ) {
        readingService.deleteReading(slotId, readTimestamp);
        return ResponseEntity.noContent().build();
    }
}
