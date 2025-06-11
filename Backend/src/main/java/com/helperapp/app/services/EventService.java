package com.helperapp.app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Event;
import com.helperapp.app.repositories.EventRepository;
import com.helperapp.app.security.JwtHelper;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtHelper jwtHelper;

    public List<Event> getAllEvents() {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return eventRepository.findAll().stream()
                .filter(e -> e.getUserId().equals(currentUserId))
                .collect(Collectors.toList());
    }

    public Optional<Event> getEventById(String id) {
        Optional<Event> event = eventRepository.findById(id);
        String currentUserId = jwtHelper.getUserIdFromToken();
        return event.filter(e -> e.getUserId().equals(currentUserId));
    }

    public Event createEvent(Event event) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        event.setUserId(currentUserId); // Associate event with the user

        LocalDate start = event.getStartDate();
        LocalDate end = event.getEndDate();

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date.");
        }

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
        String currentUserId = jwtHelper.getUserIdFromToken();

        return eventRepository.findById(id).filter(e -> e.getUserId().equals(currentUserId)).map(event -> {
            LocalDate start = updatedEvent.getStartDate();
            LocalDate end = updatedEvent.getEndDate();

            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must be before or equal to end date.");
            }

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
        String currentUserId = jwtHelper.getUserIdFromToken();
        Optional<Event> event = eventRepository.findById(id);

        if (event.isPresent() && event.get().getUserId().equals(currentUserId)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Event> updateEventDays(String id, List<Map<LocalDate, Number>> newEventDays) {
        String currentUserId = jwtHelper.getUserIdFromToken();

        return eventRepository.findById(id)
                .filter(e -> e.getUserId().equals(currentUserId))
                .map(event -> {
                    event.setEventDays(newEventDays);
                    return eventRepository.save(event);
                });
    }

    public Optional<Boolean> getHelperRegistrationStatus(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return eventRepository.findById(id)
                .filter(e -> e.getUserId().equals(currentUserId))
                .map(Event::isHelperRegistrationOpen);
    }

    public Optional<Event> setHelperRegistrationStatus(String id, boolean status) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return eventRepository.findById(id)
                .filter(e -> e.getUserId().equals(currentUserId))
                .map(event -> {
                    event.setHelperRegistrationOpen(status);
                    return eventRepository.save(event);
                });
    }
}
