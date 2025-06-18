package com.helperapp.app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helperapp.app.models.Station;

public interface StationRepository extends MongoRepository<Station, String>{
    List<Station> findByUserId(String userId);
    List<Station> findByEventId(String eventId);
    List<Station> findByUserIdAndEventId(String userId, String eventId);
}
