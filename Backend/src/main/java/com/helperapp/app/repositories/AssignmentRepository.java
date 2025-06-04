package com.helperapp.app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.helperapp.app.models.Assignment;

public interface AssignmentRepository extends MongoRepository<Assignment, String>{
    boolean existsByHelperIdAndEventDayAndEventId(String helperId, String eventDay, String eventId);
    Optional<Assignment> findByEventIdAndEventDayAndHelperId(String eventId, String eventDay, String helperId);
    List<Assignment> findByEventId(String eventId);
}
