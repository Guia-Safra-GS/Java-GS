package br.com.fiap.GuiaSafra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// Subclasse concreta da HERANCA: alerta gerado pela logica de dominio (umidade critica / risco de geada).
// Persiste em TB_MON_ALERT. Aqui o instante herdado (eventTime) e remapeado para a coluna ALERT_TIME
// via @AttributeOverride - demonstrando que a subclasse pode redefinir o mapeamento herdado.
@Entity
@Table(name = "TB_MON_ALERT")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AttributeOverride(name = "eventTime", column = @Column(name = "alert_time", nullable = false))
public class Alert extends Event {

    private String alertType;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private String message;

    // CHAR(1) 'S'/'N' no banco <-> Boolean no Java (ver YesNoConverter).
    // @JdbcTypeCode(CHAR) alinha o tipo JDBC ao CHAR(1) da coluna (o conversor sozinho
    // mapearia como VARCHAR e o ddl-auto=validate reprovaria CHAR vs VARCHAR2).
    @Convert(converter = YesNoConverter.class)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 1, nullable = false)
    private Boolean resolved;
}
