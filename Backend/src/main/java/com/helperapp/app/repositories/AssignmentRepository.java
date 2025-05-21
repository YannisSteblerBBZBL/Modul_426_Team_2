package com.helperapp.app.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helperapp.app.models.Assignment;

public interface AssignmentRepository extends MongoRepository<Assignment, Object>{
    
}
