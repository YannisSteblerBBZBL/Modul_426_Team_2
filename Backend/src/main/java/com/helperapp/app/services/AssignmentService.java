package com.helperapp.app.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Assignment;
import com.helperapp.app.models.Event;
import com.helperapp.app.repositories.AssignmentRepository;
import com.helperapp.app.repositories.EventRepository;
import com.helperapp.app.security.JwtHelper;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtHelper jwtHelper;

    public List<Assignment> getAllAssignments() {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return assignmentRepository.findAll().stream().filter(a -> a.getUserId().equals(currentUserId)).collect(Collectors.toList());
    }

    public List<Assignment> getAssignmentsByEventId(String eventId) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        
        // First verify that the user owns this event
        Event event = eventRepository.findById(eventId)
            .filter(e -> e.getUserId().equals(currentUserId))
            .orElseThrow(() -> new SecurityException("Access denied: You don't own this event."));
            
        // Then return all assignments for this event
        return assignmentRepository.findAll().stream()
            .filter(a -> a.getEventId().equals(eventId))
            .collect(Collectors.toList());
    }

    public Optional<Assignment> getAssignmentById(String id) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);
        String currentUserId = jwtHelper.getUserIdFromToken();
        return assignment.filter(a -> a.getUserId().equals(currentUserId));
    }

    public Assignment createAssignment(Assignment assignment) {
        assignment.setUserId(jwtHelper.getUserIdFromToken());
        validateAssignment(assignment);
        return assignmentRepository.save(assignment);
    }

    public Optional<Assignment> updateAssignment(String id, Assignment updatedDetails) {
        Optional<Assignment> optionalAssignment = assignmentRepository.findById(id);

        if (optionalAssignment.isEmpty()) {
            return Optional.empty();
        }

        Assignment existing = optionalAssignment.get();
        String currentUserId = jwtHelper.getUserIdFromToken();

        if (!existing.getUserId().equals(currentUserId)) {
            throw new SecurityException("Access denied: You are not allowed to update this assignment.");
        }
        
        // Only check for conflicts if the key fields actually change
        boolean isEventDayChanged = !existing.getEventDay().equals(updatedDetails.getEventDay());
        boolean isHelperChanged = !existing.getHelperId().equals(updatedDetails.getHelperId());

        if (isEventDayChanged || isHelperChanged) {
            // Check if another assignment exists for this helper on the same eventId and new eventDay
            Optional<Assignment> conflict = assignmentRepository.findByEventIdAndEventDayAndHelperId(
                    updatedDetails.getEventId(),
                    updatedDetails.getEventDay(),
                    updatedDetails.getHelperId()
            );

            if (conflict.isPresent() && !conflict.get().getId().equals(id)) {
                throw new IllegalArgumentException("This helper is already assigned to a station on this event day.");
            }
        }

        // Optional: validate that the new eventDay exists for the event
        Event event = eventRepository.findById(updatedDetails.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found."));

        List<Map<LocalDate, Number>> days = event.getEventDays();
        boolean eventDayExists = days.stream()
                .anyMatch(map -> map.values().stream()
                .anyMatch(num -> String.valueOf(num.intValue()).equals(updatedDetails.getEventDay())));

        if (!eventDayExists) {
            throw new IllegalArgumentException("Invalid event day. Event has only " + days.size()
                    + " days on the following dates: " + days.stream().map(Map::keySet).toList());
        }

        // Update fields
        existing.setEventDay(updatedDetails.getEventDay());
        existing.setHelperId(updatedDetails.getHelperId());
        existing.setStationId(updatedDetails.getStationId());

        return Optional.of(assignmentRepository.save(existing));
    }

    public boolean deleteAssignment(String id) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);
        String currentUserId = jwtHelper.getUserIdFromToken();

        if (assignment.isPresent() && assignment.get().getUserId().equals(currentUserId)) {
            assignmentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validateAssignment(Assignment assignment) {
        Optional<Event> eventOpt = eventRepository.findById(assignment.getEventId());

        if (eventOpt.isEmpty()) {
            throw new IllegalArgumentException("Event with ID " + assignment.getEventId() + " not found.");
        }

        Event event = eventOpt.get();
        List<Map<LocalDate, Number>> eventDays = event.getEventDays();

        boolean isValidDay = eventDays.stream()
                .anyMatch(dayMap -> dayMap.values().stream()
                .anyMatch(val -> String.valueOf(val.intValue()).equals(assignment.getEventDay())));

        if (!isValidDay) {
            List<String> validDates = eventDays.stream()
                    .map(dayMap -> dayMap.keySet().iterator().next().toString())
                    .toList();

            throw new IllegalArgumentException(
                    "Invalid event day. Event has only " + eventDays.size()
                    + " days on the following dates: " + validDates
            );
        }

        // Optional: Prevent same helper from being assigned twice to same eventDay
        boolean duplicateExists = assignmentRepository.existsByHelperIdAndEventDayAndEventId(
                assignment.getHelperId(), assignment.getEventDay(), assignment.getEventId()
        );

        if (duplicateExists) {
            throw new IllegalArgumentException("This helper is already assigned on this event day.");
        }
    }
}
