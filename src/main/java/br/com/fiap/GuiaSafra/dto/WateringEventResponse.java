package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.Slot;
import br.com.fiap.GuiaSafra.entity.WateringEvent;
import br.com.fiap.GuiaSafra.entity.WateringOrigin;

import java.time.LocalDateTime;

public record WateringEventResponse(
        Long id,
        Long slotId,
        Long userId,
        WateringOrigin origin,
        Integer volumeMl,
        LocalDateTime eventTime,
        SlotSummary slot
) {
    // Resumo read-only do Slot (dominio C#); evita expor a entity inteira.
    public record SlotSummary(Long id, String position, String status) {
        public static SlotSummary fromEntity(Slot slot) {
            return slot == null ? null
                    : new SlotSummary(slot.getId(), slot.getPosition(), slot.getStatus());
        }
    }

    public static WateringEventResponse fromEntity(WateringEvent event) {
        return new WateringEventResponse(
                event.getId(),
                event.getSlot() != null ? event.getSlot().getId() : null,
                event.getUserId(),
                event.getOrigin(),
                event.getVolumeMl(),
                event.getEventTime(),
                SlotSummary.fromEntity(event.getSlot())
        );
    }
}
