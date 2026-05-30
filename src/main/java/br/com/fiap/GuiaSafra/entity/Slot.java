package br.com.fiap.GuiaSafra.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalDateTime;

// TB_CAD_SLOT pertence ao C# (.NET escreve). O Java apenas LE para validar a FK
// e exibir contexto (posicao/status). @Immutable garante que o Hibernate nunca emita UPDATE.
// Sem @GeneratedValue de proposito: nao inserimos slots por aqui.
@Entity
@Immutable
@Getter
@NoArgsConstructor
@Table(name = "TB_CAD_SLOT")
public class Slot {

    @Id
    private Long id;

    private Long speciesId;

    private String position;

    private LocalDate plantedAt;

    private String status;

    private LocalDateTime createdAt;
}
