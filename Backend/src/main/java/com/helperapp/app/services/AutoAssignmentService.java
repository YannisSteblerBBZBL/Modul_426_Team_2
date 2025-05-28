package com.helperapp.app.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

        Event event = eventRepository.findById(eventId)
            .filter(e -> e.getUserId().equals(currentUserId))
            .orElseThrow(() -> new SecurityException("Access denied: You don't own this event."));

        List<Helper> helpers = helperRepository.findAll().stream()
            .filter(h -> h.getUserId().equals(currentUserId))
            .toList();

        List<Station> stations = stationRepository.findAll().stream()
            .filter(s -> s.getUserId().equals(currentUserId))
            .toList();

        // Mapping: dayNumber -> date
        Map<Integer, LocalDate> dayNumberToDateMap = new HashMap<>();
        for (Map<LocalDate, Number> dayEntry : event.getEventDays()) {
            for (Map.Entry<LocalDate, Number> entry : dayEntry.entrySet()) {
                int dayNumber = entry.getValue().intValue(); 
                dayNumberToDateMap.put(dayNumber, entry.getKey());
            }
        }

        for (Map.Entry<Integer, LocalDate> eventDayEntry : dayNumberToDateMap.entrySet()) {
            int dayNumber = eventDayEntry.getKey();

            List<Helper> availableHelpers = new ArrayList<>(helpers);

            for (Station station : stations) {
                int needed = station.getNeededHelpers().intValue();
                int assigned = 0;

                // Preferred helpers
                for (Iterator<Helper> iterator = availableHelpers.iterator(); iterator.hasNext();) {
                    if (assigned >= needed) break;

                    Helper helper = iterator.next();
                    if (!helper.getPresence().contains(dayNumber)) continue;

                    int age = Integer.parseInt(helper.getAge());
                    if (station.getIs18Plus() && age < 18) continue;

                    boolean alreadyAssigned = assignmentRepository.existsByHelperIdAndEventDayAndEventId(
                            helper.getId(), String.valueOf(dayNumber), eventId);
                    if (alreadyAssigned) continue;

                    if (helper.getPreferences() != null && helper.getPreferences().contains(station.getName())) {
                        saveAssignment(eventId, dayNumber, helper.getId(), station.getId(), currentUserId);
                        assigned++;
                        iterator.remove();
                    }
                }

                // Fallback: any available helper
                for (Iterator<Helper> iterator = availableHelpers.iterator(); iterator.hasNext();) {
                    if (assigned >= needed) break;

                    Helper helper = iterator.next();
                    if (!helper.getPresence().contains(dayNumber)) continue;

                    int age = Integer.parseInt(helper.getAge());
                    if (station.getIs18Plus() && age < 18) continue;

                    boolean alreadyAssigned = assignmentRepository.existsByHelperIdAndEventDayAndEventId(
                            helper.getId(), String.valueOf(dayNumber), eventId);
                    if (alreadyAssigned) continue;

                    saveAssignment(eventId, dayNumber, helper.getId(), station.getId(), currentUserId);
                    assigned++;
                    iterator.remove();
                }
            }
        }
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
