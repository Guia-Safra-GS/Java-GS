package br.com.fiap.GuiaSafra.controller;

import br.com.fiap.GuiaSafra.dto.AlertRequest;
import br.com.fiap.GuiaSafra.dto.AlertResponse;
import br.com.fiap.GuiaSafra.service.AlertService;
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
@RequestMapping("/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Alertas do dominio (umidade crítica / risco de geada). Subclasse concreta de Event (herança @MappedSuperclass), com filtro por slot e situação, paginação e HATEOAS")
public class AlertController {
    private final AlertService alertService;

    private EntityModel<AlertResponse> toModel(AlertResponse response) {
        return EntityModel.of(response,
                linkTo(methodOn(AlertController.class).findOne(response.id())).withSelfRel(),
                linkTo(methodOn(AlertController.class).findAll(null, null, Pageable.unpaged())).withRel("alertas"),
                linkTo(methodOn(AlertController.class).findAll(response.slotId(), null, Pageable.unpaged())).withRel("alertas-do-slot"));
    }

    @GetMapping
    @Operation(summary = "Lista alertas com paginação e filtros opcionais por slotId e resolved")
    public Page<EntityModel<AlertResponse>> findAll(
            @RequestParam(required = false) Long slotId,
            @RequestParam(required = false) Boolean resolved,
            Pageable pageable
    ) {
        return alertService.searchAlerts(slotId, resolved, pageable)
                .map(AlertResponse::fromEntity)
                .map(this::toModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um alerta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerta encontrado"),
            @ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public EntityModel<AlertResponse> findOne(@PathVariable Long id) {
        return toModel(AlertResponse.fromEntity(alertService.findAlert(id)));
    }

    @PostMapping
    @Operation(summary = "Registra um novo alerta (nasce em aberto; se alertTime for omitido, usa o instante atual)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alerta registrado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (alertType, severity, slotId)"),
            @ApiResponse(responseCode = "404", description = "Slot informado não existe")
    })
    public ResponseEntity<EntityModel<AlertResponse>> create(@RequestBody @Valid AlertRequest request) {
        EntityModel<AlertResponse> model =
                toModel(AlertResponse.fromEntity(alertService.createAlert(request)));
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @PutMapping("/{id}/resolver")
    @Operation(summary = "Marca o alerta como resolvido (RESOLVED = 'S')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerta resolvido"),
            @ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public EntityModel<AlertResponse> resolve(@PathVariable Long id) {
        return toModel(AlertResponse.fromEntity(alertService.resolveAlert(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um alerta")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alerta removido"),
            @ApiResponse(responseCode = "404", description = "Alerta não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}
