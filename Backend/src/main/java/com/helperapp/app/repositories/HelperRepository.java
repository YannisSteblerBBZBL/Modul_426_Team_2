package com.helperapp.app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helperapp.app.models.Helper;

public interface HelperRepository extends MongoRepository<Helper, String> {
    
    List<Helper> findByEventId(String eventId);
    
    List<Helper> findByUserId(String userId);
    
    List<Helper> findByUserIdAndEventId(String userId, String eventId);
    
    // Find all helpers for a user, including those without eventId
    List<Helper> findByUserIdOrderByFirstnameAsc(String userId);
}
