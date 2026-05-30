package br.com.fiap.GuiaSafra.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

// Chave composta da leitura: a identidade natural e "qual slot + em que momento".
// @Embeddable + Serializable + equals/hashCode (gerados pelo @Data) sao requisitos do JPA
// para uma classe de chave composta usada via @EmbeddedId.
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingId implements Serializable {

    private Long slotId;

    private LocalDateTime readTimestamp;
}
