package br.com.fiap.GuiaSafra.repository;

import br.com.fiap.GuiaSafra.entity.WateringEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WateringEventRepository extends JpaRepository<WateringEvent, Long> {

    // Navega pela associacao (e.slot.id) com filtro opcional por slot.
    @Query("SELECT e FROM WateringEvent e WHERE :slotId IS NULL OR e.slot.id = :slotId")
    Page<WateringEvent> search(@Param("slotId") Long slotId, Pageable pageable);
}
