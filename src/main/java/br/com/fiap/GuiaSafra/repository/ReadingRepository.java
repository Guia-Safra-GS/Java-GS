package br.com.fiap.GuiaSafra.repository;

import br.com.fiap.GuiaSafra.entity.Reading;
import br.com.fiap.GuiaSafra.entity.ReadingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// O tipo do ID e a propria chave composta (ReadingId).
public interface ReadingRepository extends JpaRepository<Reading, ReadingId> {

    // Navega pela chave composta: id.slotId. Lista o historico de um slot especifico.
    Page<Reading> findByIdSlotId(Long slotId, Pageable pageable);
}
