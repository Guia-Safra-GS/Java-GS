package br.com.fiap.GuiaSafra.entity;

// Espelha a constraint CK_WATERING_ORIGIN da TB_MON_WATERING_EVENT: ORIGIN IN ('MANUAL','AUTOMATIC')
// MANUAL = rega acionada pelo produtor via app; AUTOMATIC = rega disparada pela regra PL/SQL.
public enum WateringOrigin {
    MANUAL,
    AUTOMATIC
}
