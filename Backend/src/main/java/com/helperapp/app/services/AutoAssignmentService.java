package com.helperapp.app.services;

import java.time.LocalDate;
import java.util.*;
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

            // Filter available helpers for this day
            List<Helper> availableHelpers = allHelpers.stream()
                .filter(h -> h.getPresence().contains(dayNumber))
                .filter(h -> !assignedHelperIds.contains(h.getId()))
                .collect(Collectors.toList());

            // Process each station
            for (Station station : allStations) {
                int neededHelpers = station.getNeededHelpers().intValue();
                int currentlyAssigned = (int) existingAssignments.stream()
                    .filter(a -> a.getEventDay().equals(dayNumberStr))
                    .filter(a -> a.getStationId().equals(station.getId()))
                    .count();

                if (currentlyAssigned >= neededHelpers) {
                    continue; // Skip if station is already fully staffed
                }

                int remainingNeeded = neededHelpers - currentlyAssigned;

                // First pass: Assign preferred helpers
                List<Helper> preferredHelpers = availableHelpers.stream()
                    .filter(h -> isHelperEligibleForStation(h, station))
                    .filter(h -> h.getPreferences() != null && h.getPreferences().contains(station.getName()))
                    .collect(Collectors.toList());

                for (Helper helper : preferredHelpers) {
                    if (remainingNeeded <= 0) break;
                    
                    saveAssignment(eventId, dayNumber, helper.getId(), station.getId(), currentUserId);
                    availableHelpers.remove(helper);
                    remainingNeeded--;
                }

                // Second pass: Fill remaining spots with any eligible helper
                if (remainingNeeded > 0) {
                    List<Helper> eligibleHelpers = availableHelpers.stream()
                        .filter(h -> isHelperEligibleForStation(h, station))
                        .collect(Collectors.toList());

                    for (Helper helper : eligibleHelpers) {
                        if (remainingNeeded <= 0) break;

                        saveAssignment(eventId, dayNumber, helper.getId(), station.getId(), currentUserId);
                        availableHelpers.remove(helper);
                        remainingNeeded--;
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
