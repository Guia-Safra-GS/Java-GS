package br.com.fiap.GuiaSafra.repository;

import br.com.fiap.GuiaSafra.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Filtros opcionais por slot e por situacao (resolvido/em aberto).
    @Query("SELECT a FROM Alert a WHERE " +
            "(:slotId IS NULL OR a.slot.id = :slotId) AND " +
            "(:resolved IS NULL OR a.resolved = :resolved)")
    Page<Alert> search(@Param("slotId") Long slotId,
                       @Param("resolved") Boolean resolved,
                       Pageable pageable);
}
