package com.helperapp.app.services;

import java.time.LocalDate;
import java.util.ArrayList;
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

        // Fetch all required data upfront - only event-specific helpers and stations
        List<Helper> allHelpers = helperRepository.findByUserIdAndEventId(currentUserId, eventId);

        List<Station> allStations = stationRepository.findByUserIdAndEventId(currentUserId, eventId);

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

        // Create comprehensive assignment plan
        Map<Integer, List<AssignmentPlan>> eventAssignmentPlan = createEventAssignmentPlan(
            allHelpers, allStations, dayNumberToDateMap, existingAssignmentsByDay, eventId);

        // Execute the assignment plan
        executeAssignmentPlan(eventId, eventAssignmentPlan, currentUserId);
    }

    private Map<Integer, List<AssignmentPlan>> createEventAssignmentPlan(
            List<Helper> allHelpers, 
            List<Station> allStations, 
            Map<Integer, LocalDate> dayNumberToDateMap,
            Map<String, Set<String>> existingAssignmentsByDay,
            String eventId) {
        
        Map<Integer, List<AssignmentPlan>> eventPlan = new HashMap<>();
        
        // Initialize plan for each day
        for (Integer dayNumber : dayNumberToDateMap.keySet()) {
            eventPlan.put(dayNumber, new ArrayList<>());
        }

        // Step 1: Analyze helper availability and preferences
        Map<String, HelperAvailability> helperAvailabilityMap = analyzeHelperAvailability(
            allHelpers, dayNumberToDateMap.keySet(), existingAssignmentsByDay);

        // Step 2: Calculate station requirements
        Map<Integer, Map<String, Integer>> stationRequirements = calculateStationRequirements(
            allStations, dayNumberToDateMap.keySet(), existingAssignmentsByDay, eventId);

        // Step 3: Create optimal assignments considering the entire event
        createOptimalAssignments(helperAvailabilityMap, stationRequirements, eventPlan, allStations);

        return eventPlan;
    }

    private Map<String, HelperAvailability> analyzeHelperAvailability(
            List<Helper> helpers, 
            Set<Integer> eventDays, 
            Map<String, Set<String>> existingAssignmentsByDay) {
        
        Map<String, HelperAvailability> availabilityMap = new HashMap<>();
        
        for (Helper helper : helpers) {
            HelperAvailability availability = new HelperAvailability();
            availability.helper = helper;
            availability.availableDays = new HashSet<>();
            availability.assignedDays = new HashSet<>();
            availability.preferredStations = helper.getPreferencedStations() != null ? 
                new HashSet<>(helper.getPreferencedStations()) : new HashSet<>();
            
            // Determine available days
            if (helper.getPresence() != null) {
                for (Number presenceDay : helper.getPresence()) {
                    int dayNum = presenceDay.intValue();
                    if (eventDays.contains(dayNum)) {
                        availability.availableDays.add(dayNum);
                    }
                }
            }
            
            // Check already assigned days
            String helperId = helper.getId();
            for (Map.Entry<String, Set<String>> entry : existingAssignmentsByDay.entrySet()) {
                if (entry.getValue().contains(helperId)) {
                    availability.assignedDays.add(Integer.parseInt(entry.getKey()));
                }
            }
            
            availabilityMap.put(helperId, availability);
        }
        
        return availabilityMap;
    }

    private Map<Integer, Map<String, Integer>> calculateStationRequirements(
            List<Station> stations, 
            Set<Integer> eventDays, 
            Map<String, Set<String>> existingAssignmentsByDay,
            String eventId) {
        
        Map<Integer, Map<String, Integer>> requirements = new HashMap<>();
        
        for (Integer dayNumber : eventDays) {
            Map<String, Integer> dayRequirements = new HashMap<>();
            String dayNumberStr = String.valueOf(dayNumber);
            
            for (Station station : stations) {
                int needed = station.getNeededHelpers().intValue();
                
                // Count existing assignments for this station on this day
                int alreadyAssigned = (int) assignmentRepository.findByEventId(eventId).stream()
                    .filter(a -> a.getEventDay().equals(dayNumberStr))
                    .filter(a -> a.getStationId().equals(station.getId()))
                    .count();
                
                int remaining = Math.max(0, needed - alreadyAssigned);
                if (remaining > 0) {
                    dayRequirements.put(station.getId(), remaining);
                }
            }
            
            requirements.put(dayNumber, dayRequirements);
        }
        
        return requirements;
    }

    private void createOptimalAssignments(
            Map<String, HelperAvailability> helperAvailabilityMap,
            Map<Integer, Map<String, Integer>> stationRequirements,
            Map<Integer, List<AssignmentPlan>> eventPlan,
            List<Station> allStations) {
        
        // Phase 1: Assign helpers to their preferred stations first
        assignPreferredStations(helperAvailabilityMap, stationRequirements, eventPlan, allStations);
        
        // Phase 2: Fill remaining positions with optimal distribution
        fillRemainingPositions(helperAvailabilityMap, stationRequirements, eventPlan, allStations);
        
        // Phase 3: Balance assignments across days
        balanceAssignmentsAcrossDays(helperAvailabilityMap, eventPlan, allStations);
    }

    private void assignPreferredStations(
            Map<String, HelperAvailability> helperAvailabilityMap,
            Map<Integer, Map<String, Integer>> stationRequirements,
            Map<Integer, List<AssignmentPlan>> eventPlan,
            List<Station> allStations) {
        
        // Sort helpers by preference strength (more preferences = higher priority)
        List<HelperAvailability> sortedHelpers = helperAvailabilityMap.values().stream()
            .filter(ha -> !ha.preferredStations.isEmpty())
            .sorted((ha1, ha2) -> Integer.compare(ha2.preferredStations.size(), ha1.preferredStations.size()))
            .collect(Collectors.toList());
        
        for (HelperAvailability helperAvail : sortedHelpers) {
            Helper helper = helperAvail.helper;
            
            // Find best day and station for this helper
            AssignmentPlan bestAssignment = findBestPreferredAssignment(
                helperAvail, stationRequirements, allStations);
            
            if (bestAssignment != null) {
                // Add to plan
                eventPlan.get(bestAssignment.dayNumber).add(bestAssignment);
                
                // Update availability and requirements
                helperAvail.assignedDays.add(bestAssignment.dayNumber);
                updateStationRequirements(stationRequirements, bestAssignment.stationId, 
                    bestAssignment.dayNumber, -1);
            }
        }
    }

    private AssignmentPlan findBestPreferredAssignment(
            HelperAvailability helperAvail,
            Map<Integer, Map<String, Integer>> stationRequirements,
            List<Station> allStations) {
        
        Helper helper = helperAvail.helper;
        AssignmentPlan bestAssignment = null;
        int bestScore = -1;
        
        for (Integer dayNumber : helperAvail.availableDays) {
            if (helperAvail.assignedDays.contains(dayNumber)) {
                continue; // Already assigned on this day
            }
            
            Map<String, Integer> dayRequirements = stationRequirements.get(dayNumber);
            if (dayRequirements == null) continue;
            
            for (String preferredStationId : helperAvail.preferredStations) {
                if (!dayRequirements.containsKey(preferredStationId)) continue;
                
                Station station = allStations.stream()
                    .filter(s -> s.getId().equals(preferredStationId))
                    .findFirst().orElse(null);
                
                if (station != null && isHelperEligibleForStation(helper, station)) {
                    // Calculate score based on various factors
                    int score = calculateAssignmentScore(helperAvail, dayNumber, station, dayRequirements);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestAssignment = new AssignmentPlan(helper.getId(), preferredStationId, dayNumber);
                    }
                }
            }
        }
        
        return bestAssignment;
    }

    private int calculateAssignmentScore(
            HelperAvailability helperAvail, 
            int dayNumber, 
            Station station, 
            Map<String, Integer> dayRequirements) {
        
        int score = 0;
        
        // Base score for preference match
        score += 100;
        
        // Bonus for stations with higher need
        score += dayRequirements.get(station.getId()) * 10;
        
        // Bonus for helpers with fewer available days (prioritize them)
        score += (10 - helperAvail.availableDays.size()) * 5;
        
        // Penalty for helpers already assigned on many days
        score -= helperAvail.assignedDays.size() * 2;
        
        return score;
    }

    private void fillRemainingPositions(
            Map<String, HelperAvailability> helperAvailabilityMap,
            Map<Integer, Map<String, Integer>> stationRequirements,
            Map<Integer, List<AssignmentPlan>> eventPlan,
            List<Station> allStations) {
        
        // Get unassigned helpers
        List<HelperAvailability> unassignedHelpers = helperAvailabilityMap.values().stream()
            .filter(ha -> ha.assignedDays.size() < ha.availableDays.size())
            .sorted((ha1, ha2) -> {
                // Prioritize helpers with fewer assignments
                int diff = ha1.assignedDays.size() - ha2.assignedDays.size();
                if (diff != 0) return diff;
                // Then by fewer available days
                return ha1.availableDays.size() - ha2.availableDays.size();
            })
            .collect(Collectors.toList());
        
        for (HelperAvailability helperAvail : unassignedHelpers) {
            Helper helper = helperAvail.helper;
            
            // Find any available assignment
            AssignmentPlan assignment = findAnyAvailableAssignment(
                helperAvail, stationRequirements, allStations);
            
            if (assignment != null) {
                eventPlan.get(assignment.dayNumber).add(assignment);
                helperAvail.assignedDays.add(assignment.dayNumber);
                updateStationRequirements(stationRequirements, assignment.stationId, 
                    assignment.dayNumber, -1);
            }
        }
    }

    private AssignmentPlan findAnyAvailableAssignment(
            HelperAvailability helperAvail,
            Map<Integer, Map<String, Integer>> stationRequirements,
            List<Station> allStations) {
        
        Helper helper = helperAvail.helper;
        
        for (Integer dayNumber : helperAvail.availableDays) {
            if (helperAvail.assignedDays.contains(dayNumber)) continue;
            
            Map<String, Integer> dayRequirements = stationRequirements.get(dayNumber);
            if (dayRequirements == null) continue;
            
            for (Map.Entry<String, Integer> requirement : dayRequirements.entrySet()) {
                String stationId = requirement.getKey();
                Station station = allStations.stream()
                    .filter(s -> s.getId().equals(stationId))
                    .findFirst().orElse(null);
                
                if (station != null && isHelperEligibleForStation(helper, station)) {
                    return new AssignmentPlan(helper.getId(), stationId, dayNumber);
                }
            }
        }
        
        return null;
    }

    private void balanceAssignmentsAcrossDays(
            Map<String, HelperAvailability> helperAvailabilityMap,
            Map<Integer, List<AssignmentPlan>> eventPlan,
            List<Station> allStations) {
        
        // This phase can be used to optimize the distribution further
        // For now, we'll implement a simple balancing mechanism
        
        Map<Integer, Integer> assignmentsPerDay = new HashMap<>();
        for (Map.Entry<Integer, List<AssignmentPlan>> entry : eventPlan.entrySet()) {
            assignmentsPerDay.put(entry.getKey(), entry.getValue().size());
        }
        
        // Find days with too many or too few assignments
        int avgAssignments = assignmentsPerDay.values().stream()
            .mapToInt(Integer::intValue).sum() / assignmentsPerDay.size();
        
        // Simple balancing: if a day has significantly more assignments than average,
        // try to move some to days with fewer assignments
        // This is a simplified approach - in practice, you might want more sophisticated balancing
    }

    private void updateStationRequirements(
            Map<Integer, Map<String, Integer>> stationRequirements,
            String stationId,
            int dayNumber,
            int change) {
        
        Map<String, Integer> dayRequirements = stationRequirements.get(dayNumber);
        if (dayRequirements != null && dayRequirements.containsKey(stationId)) {
            int current = dayRequirements.get(stationId);
            int newValue = Math.max(0, current + change);
            if (newValue == 0) {
                dayRequirements.remove(stationId);
            } else {
                dayRequirements.put(stationId, newValue);
            }
        }
    }

    private void executeAssignmentPlan(
            String eventId, 
            Map<Integer, List<AssignmentPlan>> eventPlan, 
            String userId) {
        
        for (Map.Entry<Integer, List<AssignmentPlan>> entry : eventPlan.entrySet()) {
            int dayNumber = entry.getKey();
            List<AssignmentPlan> dayAssignments = entry.getValue();
            
            for (AssignmentPlan plan : dayAssignments) {
                saveAssignment(eventId, dayNumber, plan.helperId, plan.stationId, userId);
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

    // Helper classes for the assignment planning
    private static class HelperAvailability {
        Helper helper;
        Set<Integer> availableDays;
        Set<Integer> assignedDays;
        Set<String> preferredStations;
    }

    private static class AssignmentPlan {
        String helperId;
        String stationId;
        int dayNumber;

        AssignmentPlan(String helperId, String stationId, int dayNumber) {
            this.helperId = helperId;
            this.stationId = stationId;
            this.dayNumber = dayNumber;
        }
    }
} 