package br.com.fiap.GuiaSafra.service;

import br.com.fiap.GuiaSafra.dto.WateringEventRequest;
import br.com.fiap.GuiaSafra.entity.Slot;
import br.com.fiap.GuiaSafra.entity.WateringEvent;
import br.com.fiap.GuiaSafra.repository.SlotRepository;
import br.com.fiap.GuiaSafra.repository.UserRepository;
import br.com.fiap.GuiaSafra.repository.WateringEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WateringEventService {
    private final WateringEventRepository wateringEventRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    public Page<WateringEvent> searchEvents(Long slotId, Pageable pageable) {
        return wateringEventRepository.search(slotId, pageable);
    }

    public WateringEvent findEvent(Long id) {
        return wateringEventRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    public WateringEvent createEvent(WateringEventRequest request) {
        // Valida a FK do slot (dominio C#). Se nao existir -> 404.
        Slot slot = slotRepository.findById(request.slotId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Slot with ID %d not found", request.slotId()))
        );

        // userId e opcional (nulo em rega AUTOMATIC); se informado, precisa existir -> 404.
        if (request.userId() != null && !userRepository.existsById(request.userId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("User with ID %d not found", request.userId()));
        }

        LocalDateTime when = request.eventTime() != null ? request.eventTime() : LocalDateTime.now();

        WateringEvent event = WateringEvent.builder()
                .slot(slot)
                .userId(request.userId())
                .origin(request.origin())
                .volumeMl(request.volumeMl())
                .eventTime(when)
                .build();

        return wateringEventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        if (!wateringEventRepository.existsById(id)) {
            throw notFound(id);
        }
        wateringEventRepository.deleteById(id);
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                String.format("Watering event with ID %d not found", id));
    }
}
