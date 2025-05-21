package com.helperapp.app.services;

import com.helperapp.app.models.Station;
import com.helperapp.app.repositories.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StationService {

    @Autowired
    private StationRepository stationRepository;

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    public Optional<Station> getStationById(String id) {
        return stationRepository.findById(id);
    }

    public Station createStation(Station station) {
        return stationRepository.save(station);
    }

    public Optional<Station> updateStation(String id, Station updatedStation) {
        return stationRepository.findById(id).map(station -> {
            station.setName(updatedStation.getName());
            station.setIs18Plus(updatedStation.getIs18Plus());
            return stationRepository.save(station);
        });
    }

    public boolean deleteStation(String id) {
        if (stationRepository.existsById(id)) {
            stationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
