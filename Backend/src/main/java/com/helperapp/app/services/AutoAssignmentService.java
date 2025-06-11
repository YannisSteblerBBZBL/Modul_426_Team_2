package com.helperapp.app.services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Assignment;
import com.helperapp.app.models.Event;
import com.helperapp.app.models.Helper;
import com.helperapp.app.models.Station;
import com.helperapp.app.repositories.AssignmentRepository;
import com.helperapp.app.repositories.EventRepository;
import com.helperapp.app.repositories.HelperRepository;
import com.helperapp.app.repositories.StationRepository;
import com.helperapp.app.security.JwtHelper;

@Service
public class AutoAssignmentService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private HelperRepository helperRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private JwtHelper jwtHelper;

    public void generateAssignments(String eventId) {
        String currentUserId = jwtHelper.getUserIdFromToken();

        // Fetch event and validate ownership
        Event event = eventRepository.findById(eventId)
            .filter(e -> e.getUserId().equals(currentUserId))
            .orElseThrow(() -> new SecurityException("Access denied: You don't own this event."));

        // Fetch all required data upfront
        List<Helper> allHelpers = helperRepository.findAll().stream()
            .filter(h -> h.getUserId().equals(currentUserId))
            .toList();

        List<Station> allStations = stationRepository.findAll().stream()
            .filter(s -> s.getUserId().equals(currentUserId))
            .toList();

        // Get existing assignments to avoid modifying them
        List<Assignment> existingAssignments = assignmentRepository.findByEventId(eventId);
        Map<String, Set<String>> existingAssignmentsByDay = existingAssignments.stream()
            .collect(Collectors.groupingBy(Assignment::getEventDay,
                    Collectors.mapping(Assignment::getHelperId, Collectors.toSet())));

        // Create day number to date mapping
        Map<Integer, LocalDate> dayNumberToDateMap = new HashMap<>();
        for (Map<LocalDate, Number> dayEntry : event.getEventDays()) {
            dayEntry.forEach((date, dayNum) -> dayNumberToDateMap.put(dayNum.intValue(), date));
        }

        // Process each day
        for (Map.Entry<Integer, LocalDate> eventDayEntry : dayNumberToDateMap.entrySet()) {
            int dayNumber = eventDayEntry.getKey();
            String dayNumberStr = String.valueOf(dayNumber);
            
            // Get helpers already assigned for this day
            Set<String> assignedHelperIds = existingAssignmentsByDay.getOrDefault(dayNumberStr, new HashSet<>());

            // Get available helpers for this day
            List<Helper> availableHelpers = allHelpers.stream()
                .filter(h -> h.getPresence().contains(dayNumber))
                .filter(h -> !assignedHelperIds.contains(h.getId()))
                .collect(Collectors.toList());

            // Calculate remaining needs for each station
            Map<String, Integer> remainingNeeds = new HashMap<>();
            for (Station station : allStations) {
                int currentlyAssigned = (int) existingAssignments.stream()
                    .filter(a -> a.getEventDay().equals(dayNumberStr))
                    .filter(a -> a.getStationId().equals(station.getId()))
                    .count();
                int needed = station.getNeededHelpers().intValue() - currentlyAssigned;
                if (needed > 0) {
                    remainingNeeds.put(station.getId(), needed);
                }
            }

            // First pass: Assign helpers to their preferred stations
            Set<String> assignedHelpers = new HashSet<>();
            for (Helper helper : availableHelpers) {
                if (helper.getPreferencedStations() != null && !helper.getPreferencedStations().isEmpty()) {
                    for (String preferredStationId : helper.getPreferencedStations()) {
                        if (remainingNeeds.containsKey(preferredStationId)) {
                            Station station = allStations.stream()
                                .filter(s -> s.getId().equals(preferredStationId))
                                .findFirst()
                                .orElse(null);

                            if (station != null && isHelperEligibleForStation(helper, station)) {
                                // Assign helper to their preferred station
                                saveAssignment(eventId, dayNumber, helper.getId(), preferredStationId, currentUserId);
                                assignedHelpers.add(helper.getId());
                                int remaining = remainingNeeds.get(preferredStationId) - 1;
                                if (remaining <= 0) {
                                    remainingNeeds.remove(preferredStationId);
                                } else {
                                    remainingNeeds.put(preferredStationId, remaining);
                                }
                                break; // Helper is assigned, move to next helper
                            }
                        }
                    }
                }
            }

            // Second pass: Fill remaining positions with unassigned helpers
            List<Helper> remainingHelpers = availableHelpers.stream()
                .filter(h -> !assignedHelpers.contains(h.getId()))
                .collect(Collectors.toList());

            for (Helper helper : remainingHelpers) {
                for (Map.Entry<String, Integer> need : new HashMap<>(remainingNeeds).entrySet()) {
                    String stationId = need.getKey();
                    Station station = allStations.stream()
                        .filter(s -> s.getId().equals(stationId))
                        .findFirst()
                        .orElse(null);

                    if (station != null && isHelperEligibleForStation(helper, station)) {
                        // Assign helper to an available station
                        saveAssignment(eventId, dayNumber, helper.getId(), stationId, currentUserId);
                        int remaining = remainingNeeds.get(stationId) - 1;
                        if (remaining <= 0) {
                            remainingNeeds.remove(stationId);
                        } else {
                            remainingNeeds.put(stationId, remaining);
                        }
                        break; // Helper is assigned, move to next helper
                    }
                }
            }
        }
    }

    private boolean isHelperEligibleForStation(Helper helper, Station station) {
        int age = Integer.parseInt(helper.getAge());
        return !station.getIs18Plus() || age >= 18;
    }

    private void saveAssignment(String eventId, int dayNumber, String helperId, String stationId, String userId) {
        Assignment assignment = new Assignment();
        assignment.setEventId(eventId);
        assignment.setEventDay(String.valueOf(dayNumber));
        assignment.setHelperId(helperId);
        assignment.setStationId(stationId);
        assignment.setUserId(userId);
        assignmentRepository.save(assignment);
    }
}
