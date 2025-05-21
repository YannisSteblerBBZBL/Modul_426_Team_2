package com.helperapp.app.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helperapp.app.models.Event;

public interface EventRepository extends MongoRepository<Event, String> {
    
}
