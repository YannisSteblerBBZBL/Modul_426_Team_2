package com.helperapp.app.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Event;
import com.helperapp.app.models.Station;
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

    public List<Station> getAllStations() {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return stationRepository.findAll().stream()
                .filter(station -> station.getUserId().equals(currentUserId))
                .collect(Collectors.toList());
    }

    public Optional<Station> getStationById(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        Optional<Station> station = stationRepository.findById(id);
        return station.filter(s -> s.getUserId().equals(currentUserId));
    }

    public Station createStation(Station station) {
        station.setUserId(jwtHelper.getUserIdFromToken());
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

    public List<Station> getStationsByEventId(String eventId) {
        // Get the event to find its userId
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            return List.of(); // Return empty list if event not found
        }

        // Get all stations for this event's user
        return stationRepository.findByUserId(event.get().getUserId());
    }
}
