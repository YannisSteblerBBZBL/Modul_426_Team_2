package com.helperapp.app.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helperapp.app.models.Helper;

public interface HelperRepository extends MongoRepository<Helper, String> {
    
}
