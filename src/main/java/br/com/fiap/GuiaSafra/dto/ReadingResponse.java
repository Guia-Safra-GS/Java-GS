package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.Reading;
import br.com.fiap.GuiaSafra.entity.Slot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReadingResponse(
        Long slotId,
        LocalDateTime readTimestamp,
        BigDecimal humidity,
        BigDecimal temperature,
        SlotSummary slot
) {
    // Resumo read-only do Slot (vem do dominio C#); evita expor a entity inteira.
    public record SlotSummary(Long id, String position, String status) {
        public static SlotSummary fromEntity(Slot slot) {
            return slot == null ? null
                    : new SlotSummary(slot.getId(), slot.getPosition(), slot.getStatus());
        }
    }

    public static ReadingResponse fromEntity(Reading reading) {
        return new ReadingResponse(
                reading.getId().getSlotId(),
                reading.getId().getReadTimestamp(),
                reading.getHumidity(),
                reading.getTemperature(),
                SlotSummary.fromEntity(reading.getSlot())
        );
    }
}
