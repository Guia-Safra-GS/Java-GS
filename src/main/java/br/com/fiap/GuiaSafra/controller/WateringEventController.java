package br.com.fiap.GuiaSafra.controller;

import br.com.fiap.GuiaSafra.dto.WateringEventRequest;
import br.com.fiap.GuiaSafra.dto.WateringEventResponse;
import br.com.fiap.GuiaSafra.service.WateringEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/regas")
@RequiredArgsConstructor
@Tag(name = "Eventos de Rega", description = "Registro de regas (MANUAL pelo produtor ou AUTOMATIC pela regra do banco). Subclasse concreta de Event (herança @MappedSuperclass), com paginação e HATEOAS")
public class WateringEventController {
    private final WateringEventService wateringEventService;

    private EntityModel<WateringEventResponse> toModel(WateringEventResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(WateringEventController.class).findOne(response.id())).withSelfRel(),
                linkTo(methodOn(WateringEventController.class).findAll(null, Pageable.unpaged())).withRel("regas"),
                linkTo(methodOn(WateringEventController.class).findAll(response.slotId(), Pageable.unpaged())).withRel("regas-do-slot"));
    }

    @GetMapping
    @Operation(summary = "Lista regas com paginação e filtro opcional por slotId")
    public Page<EntityModel<WateringEventResponse>> findAll(
            @RequestParam(required = false) Long slotId,
            Pageable pageable
    ) {
        return wateringEventService.searchEvents(slotId, pageable)
                .map(WateringEventResponse::fromEntity)
                .map(this::toModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma rega por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rega encontrada"),
            @ApiResponse(responseCode = "404", description = "Rega não encontrada")
    })
    public EntityModel<WateringEventResponse> findOne(@PathVariable Long id) {
        return toModel(WateringEventResponse.fromEntity(wateringEventService.findEvent(id)));
    }

    @PostMapping
    @Operation(summary = "Registra uma nova rega (se eventTime for omitido, usa o instante atual)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rega registrada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (origin, volumeMl, slotId)"),
            @ApiResponse(responseCode = "404", description = "Slot ou usuário informado não existe")
    })
    public ResponseEntity<EntityModel<WateringEventResponse>> create(@RequestBody @Valid WateringEventRequest request) {
        EntityModel<WateringEventResponse> model =
                toModel(WateringEventResponse.fromEntity(wateringEventService.createEvent(request)));
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma rega")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rega removida"),
            @ApiResponse(responseCode = "404", description = "Rega não encontrada")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        wateringEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
