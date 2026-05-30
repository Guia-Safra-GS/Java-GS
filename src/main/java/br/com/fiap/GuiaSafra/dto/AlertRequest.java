package br.com.fiap.GuiaSafra.dto;

import br.com.fiap.GuiaSafra.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AlertRequest(
        @NotNull(message = "slotId is mandatory")
        @Positive(message = "slotId must be positive")
        Long slotId,

        @NotBlank(message = "alertType is mandatory")
        @Size(max = 20, message = "alertType must have a max of 20 characters")
        String alertType,

        @NotNull(message = "severity is mandatory (LOW, MEDIUM or CRITICAL)")
        Severity severity,

        @Size(max = 200, message = "message must have a max of 200 characters")
        String message,

        // Opcional: se omitido, o serviço usa o instante atual.
        LocalDateTime alertTime
) {
}
