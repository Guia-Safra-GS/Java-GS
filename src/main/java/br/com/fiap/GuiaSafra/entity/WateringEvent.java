package br.com.fiap.GuiaSafra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

// Subclasse concreta da HERANCA: rega de um slot. Persiste em TB_MON_WATERING_EVENT.
// Herda id, slot e eventTime de Event (EVENT_TIME bate com o nome padrao, sem override).
@Entity
@Table(name = "TB_MON_WATERING_EVENT")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class WateringEvent extends Event {

    // NULO quando a rega foi AUTOMATIC (regra do banco); preenchido quando MANUAL (produtor pelo app).
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private WateringOrigin origin;

    private Integer volumeMl;
}
