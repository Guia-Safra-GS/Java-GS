package br.com.fiap.GuiaSafra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@Builder
@Table(name = "TB_MON_READING")
@AllArgsConstructor
@NoArgsConstructor
public class Reading {

    @EmbeddedId
    private ReadingId id;

    // Associacao de navegacao para o Slot. Compartilha a coluna SLOT_ID com a chave composta,
    // por isso e read-only aqui (insertable/updatable = false): quem grava SLOT_ID e o @EmbeddedId.
    // O valor da FK e definido via id.slotId; este campo serve so para ler o Slot.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", insertable = false, updatable = false)
    private Slot slot;

    private BigDecimal humidity;

    private BigDecimal temperature;
}
