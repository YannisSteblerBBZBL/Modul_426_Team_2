package com.helperapp.app.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Assignment;
import com.helperapp.app.models.Station;
import com.helperapp.app.repositories.AssignmentRepository;
import com.helperapp.app.repositories.EventRepository;
import com.helperapp.app.repositories.StationRepository;
import com.helperapp.app.security.JwtHelper;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    public List<Station> getAllStations(String eventId) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        if (eventId != null && !eventId.isEmpty()) {
            return stationRepository.findByUserIdAndEventId(currentUserId, eventId);
        } else {
            return stationRepository.findByUserId(currentUserId);
        }
    }

    public Optional<Station> getStationById(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        Optional<Station> station = stationRepository.findById(id);
        return station.filter(s -> s.getUserId().equals(currentUserId));
    }

    public Station createStation(Station station) {
        station.setUserId(jwtHelper.getUserIdFromToken());
        if (station.getEventId() == null || station.getEventId().isEmpty()) {
            throw new IllegalArgumentException("Station muss einem Event zugeordnet werden.");
        }
        return stationRepository.save(station);
    }

    public Optional<Station> updateStation(String id, Station updatedStation) {
        String currentUserId = jwtHelper.getUserIdFromToken();

        return stationRepository.findById(id)
                .filter(s -> s.getUserId().equals(currentUserId))
                .map(station -> {
                    station.setName(updatedStation.getName());
                    station.setIs18Plus(updatedStation.getIs18Plus());
                    return stationRepository.save(station);
                });
    }

    public boolean deleteStation(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        Optional<Station> station = stationRepository.findById(id);

        if (station.isPresent() && station.get().getUserId().equals(currentUserId)) {
            stationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Assignment> getAssignmentsByStationId(String stationId) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        
        // First verify that the user owns this station
        Optional<Station> station = stationRepository.findById(stationId);
        if (station.isEmpty() || !station.get().getUserId().equals(currentUserId)) {
            return List.of(); // Return empty list if station not found or not owned by user
        }
        
        // Return all assignments for this station
        return assignmentRepository.findByStationId(stationId);
    }

    public boolean forceDeleteStation(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        Optional<Station> station = stationRepository.findById(id);

        if (station.isPresent() && station.get().getUserId().equals(currentUserId)) {
            // Delete all assignments for this station first
            List<Assignment> assignments = assignmentRepository.findByStationId(id);
            assignmentRepository.deleteAll(assignments);
            
            // Then delete the station
            stationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Station> getStationsByUserIdAndEventId(String eventId) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return stationRepository.findByUserIdAndEventId(currentUserId, eventId);
    }

    public List<Station> getStationsByEventId(String eventId) {
        return stationRepository.findByEventId(eventId);
    }
}
