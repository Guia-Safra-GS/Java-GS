package br.com.fiap.GuiaSafra.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// Converte o Boolean do Java <-> CHAR(1) da coluna RESOLVED (CK_ALERT_RESOLVED IN ('S','N')).
// No banco persistimos 'S' (sim/resolvido) ou 'N' (nao/em aberto); no Java trabalhamos com boolean.
@Converter
public class YesNoConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean value) {
        return Boolean.TRUE.equals(value) ? "S" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbValue) {
        return "S".equalsIgnoreCase(dbValue);
    }
}
