package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.Alert;
import br.com.fiap.GuiaSafra.entity.Severity;
import br.com.fiap.GuiaSafra.entity.Slot;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Long slotId,
        String alertType,
        Severity severity,
        String message,
        LocalDateTime alertTime,
        Boolean resolved,
        SlotSummary slot
) {
    // Resumo read-only do Slot (dominio C#); evita expor a entity inteira.
    public record SlotSummary(Long id, String position, String status) {
        public static SlotSummary fromEntity(Slot slot) {
            return slot == null ? null
                    : new SlotSummary(slot.getId(), slot.getPosition(), slot.getStatus());
        }
    }

    public static AlertResponse fromEntity(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getSlot() != null ? alert.getSlot().getId() : null,
                alert.getAlertType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getEventTime(),
                alert.getResolved(),
                SlotSummary.fromEntity(alert.getSlot())
        );
    }
}
