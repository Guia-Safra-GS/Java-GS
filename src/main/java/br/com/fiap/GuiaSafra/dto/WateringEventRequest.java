package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.WateringOrigin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record WateringEventRequest(
        @NotNull(message = "slotId is mandatory")
        @Positive(message = "slotId must be positive")
        Long slotId,

        // Opcional: presente quando a rega foi MANUAL (quem acionou); nulo quando AUTOMATIC.
        Long userId,

        @NotNull(message = "origin is mandatory (MANUAL or AUTOMATIC)")
        WateringOrigin origin,

        @NotNull(message = "volumeMl is mandatory")
        @Positive(message = "volumeMl must be greater than zero")
        Integer volumeMl,

        // Opcional: se omitido, o serviço usa o instante atual.
        LocalDateTime eventTime
) {
}
