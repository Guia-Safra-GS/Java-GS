package br.com.fiap.GuiaSafra.repository;

import br.com.fiap.GuiaSafra.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

// Read-only na pratica: usamos apenas para findById/existsById ao validar a FK da leitura.
public interface SlotRepository extends JpaRepository<Slot, Long> {
}
