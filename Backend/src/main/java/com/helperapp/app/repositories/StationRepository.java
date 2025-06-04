package com.helperapp.app.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helperapp.app.models.Station;

public interface StationRepository extends MongoRepository<Station, String>{
    
}
