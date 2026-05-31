package br.com.fiap.GuiaSafra.service;

import br.com.fiap.GuiaSafra.dto.AlertRequest;
import br.com.fiap.GuiaSafra.entity.Alert;
import br.com.fiap.GuiaSafra.entity.Slot;
import br.com.fiap.GuiaSafra.repository.AlertRepository;
import br.com.fiap.GuiaSafra.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final AlertRepository alertRepository;
    private final SlotRepository slotRepository;

    public Page<Alert> searchAlerts(Long slotId, Boolean resolved, Pageable pageable) {
        return alertRepository.search(slotId, resolved, pageable);
    }

    public Alert findAlert(Long id) {
        return alertRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    public Alert createAlert(AlertRequest request) {
        // Valida a FK do slot (dominio C#). Se nao existir -> 404.
        Slot slot = slotRepository.findById(request.slotId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Slot with ID %d not found", request.slotId()))
        );

        LocalDateTime when = request.alertTime() != null ? request.alertTime() : LocalDateTime.now();

        Alert alert = Alert.builder()
                .slot(slot)
                .alertType(request.alertType())
                .severity(request.severity())
                .message(request.message())
                .eventTime(when)
                .resolved(false) // nasce em aberto ('N')
                .build();

        return alertRepository.save(alert);
    }

    // Marca o alerta como resolvido ('S'). Idempotente: chamar de novo mantem resolvido.
    public Alert resolveAlert(Long id) {
        Alert alert = findAlert(id);
        alert.setResolved(true);
        return alertRepository.save(alert);
    }

    public void deleteAlert(Long id) {
        if (!alertRepository.existsById(id)) {
            throw notFound(id);
        }
        alertRepository.deleteById(id);
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Alert with ID %d not found", id));
    }
}
