package com.helperapp.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helperapp.app.models.Assignment;
import com.helperapp.app.models.Station;
import com.helperapp.app.services.StationService;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    @Autowired
    private StationService stationService;

    @GetMapping
    public List<Station> getAllStations(@RequestParam(value = "eventId", required = false) String eventId) {
        return stationService.getAllStations(eventId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable String id) {
        return stationService.getStationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Station createStation(@RequestBody Station station) {
        return stationService.createStation(station);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Station> updateStation(@PathVariable String id, @RequestBody Station station) {
        return stationService.updateStation(id, station)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable String id) {
        return stationService.deleteStation(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}/force")
    public ResponseEntity<Void> forceDeleteStation(@PathVariable String id) {
        return stationService.forceDeleteStation(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/assignments")
    public List<Assignment> getAssignmentsByStation(@PathVariable String id) {
        return stationService.getAssignmentsByStationId(id);
    }

    @GetMapping("/event/{eventId}")
    public List<Station> getStationsByEvent(@PathVariable String eventId) {
        return stationService.getStationsByEventId(eventId);
    }
}
