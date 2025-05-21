package com.helperapp.app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Event;
import com.helperapp.app.repositories.EventRepository;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(String id) {
        return eventRepository.findById(id);
    }

    public Event createEvent(Event event) {
        LocalDate start = event.getStartDate();
        LocalDate end = event.getEndDate();

        // Validation: startDate must not be after endDate
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date.");
        }

        // Generate eventDays
        List<Map<LocalDate, Number>> days = new ArrayList<>();
        LocalDate current = start;
        int dayCounter = 1;

        while (!current.isAfter(end)) {
            Map<LocalDate, Number> dayMap = new HashMap<>();
            dayMap.put(current, dayCounter++);
            days.add(dayMap);
            current = current.plusDays(1);
        }

        event.setEventDays(days);

        return eventRepository.save(event);
    }

    public Optional<Event> updateEvent(String id, Event updatedEvent) {
        return eventRepository.findById(id).map(event -> {
            LocalDate start = updatedEvent.getStartDate();
            LocalDate end = updatedEvent.getEndDate();

            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must be before or equal to end date.");
            }

            // Rebuild eventDays
            List<Map<LocalDate, Number>> days = new ArrayList<>();
            LocalDate current = start;
            int dayCounter = 1;

            while (!current.isAfter(end)) {
                Map<LocalDate, Number> dayMap = new HashMap<>();
                dayMap.put(current, dayCounter++);
                days.add(dayMap);
                current = current.plusDays(1);
            }

            event.setName(updatedEvent.getName());
            event.setDescription(updatedEvent.getDescription());
            event.setStartDate(start);
            event.setEndDate(end);
            event.setEventDays(days);

            return eventRepository.save(event);
        });
    }

    public boolean deleteEvent(String id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Event> updateEventDays(String id, List<Map<LocalDate, Number>> newEventDays) {
    return eventRepository.findById(id).map(event -> {
        event.setEventDays(newEventDays);
        return eventRepository.save(event);
    });
}
}
