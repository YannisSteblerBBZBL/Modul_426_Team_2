package com.helperapp.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helperapp.app.services.AutoAssignmentService;

@RestController
@RequestMapping("/api/auto-assignments")
public class AutoAssignmentController {

    @Autowired
    private AutoAssignmentService autoAssignmentService;

    @PostMapping("/{eventId}")
    public ResponseEntity<String> generateAssignments(@PathVariable String eventId) {
        autoAssignmentService.generateAssignments(eventId);
        return ResponseEntity.ok("Assignments generated successfully");
    }
}
