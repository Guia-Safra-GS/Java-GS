package br.com.fiap.GuiaSafra.service;

import br.com.fiap.GuiaSafra.dto.ReadingRequest;
import br.com.fiap.GuiaSafra.entity.Reading;
import br.com.fiap.GuiaSafra.entity.ReadingId;
import br.com.fiap.GuiaSafra.entity.Slot;
import br.com.fiap.GuiaSafra.repository.ReadingRepository;
import br.com.fiap.GuiaSafra.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingService {
    private final ReadingRepository readingRepository;
    private final SlotRepository slotRepository;

    public Page<Reading> searchReadings(Long slotId, Pageable pageable) {
        return slotId != null
                ? readingRepository.findByIdSlotId(slotId, pageable)
                : readingRepository.findAll(pageable);
    }

    public Reading findReading(Long slotId, LocalDateTime readTimestamp) {
        return readingRepository.findById(new ReadingId(slotId, readTimestamp)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Reading for slot %d at %s not found", slotId, readTimestamp))
        );
    }

    public Reading createReading(ReadingRequest request) {
        // Valida a FK lendo o Slot (dominio C#). Se nao existir -> 404.
        Slot slot = slotRepository.findById(request.slotId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Slot with ID %d not found", request.slotId()))
        );

        LocalDateTime timestamp = request.readTimestamp() != null
                ? request.readTimestamp()
                : LocalDateTime.now();

        ReadingId id = new ReadingId(slot.getId(), timestamp);

        // Espelha a PK composta: nao pode haver duas leituras do mesmo slot no mesmo instante -> 409.
        if (readingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Reading for slot %d at %s already exists", slot.getId(), timestamp));
        }

        Reading reading = Reading.builder()
                .id(id)
                .slot(slot)
                .humidity(request.humidity())
                .temperature(request.temperature())
                .build();

        return readingRepository.save(reading);
    }

    public void deleteReading(Long slotId, LocalDateTime readTimestamp) {
        ReadingId id = new ReadingId(slotId, readTimestamp);
        if (!readingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Reading for slot %d at %s not found", slotId, readTimestamp));
        }
        readingRepository.deleteById(id);
    }
}
