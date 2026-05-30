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

    // A coluna SLOT_ID e, ao mesmo tempo, parte da PK composta E a FK para o Slot.
    // @MapsId("slotId") amarra o atributo slotId do @EmbeddedId a esta associacao:
    // o Hibernate usa UMA unica coluna (slot_id), preenchendo id.slotId a partir do slot.
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("slotId")
    @JoinColumn(name = "slot_id")
    private Slot slot;

    private BigDecimal humidity;

    private BigDecimal temperature;
}
