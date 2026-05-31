package br.com.fiap.GuiaSafra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

// HERANCA (Modelagem Avancada) via @MappedSuperclass.
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Toda ocorrencia (rega ou alerta) pertence a um slot do C# (read-only/@Immutable).
    // A coluna fisica SLOT_ID e a mesma nas duas tabelas, entao nao precisa de override.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    // Instante do evento. O nome FISICO da coluna muda por tabela
    // (EVENT_TIME na rega, ALERT_TIME no alerta), por isso o alerta usa @AttributeOverride.
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
}
