package com.helperapp.app.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document(collection = "events")
public class Event {
    
    @Id
    private String id;

    private String userId;

    @NonNull
    private String name;

    private String description;

    @NonNull
    private LocalDate startDate;

    @NonNull
    private LocalDate endDate;

    private List<Map<LocalDate, Number>> eventDays; // List of total event days (i.g. if 3 then: 21.05.2025, 1 | 22.05.2025, 2 | 23.05.2025, 3)

    private boolean isHelperRegistrationOpen = false; // Controls if public helper registration is allowed
    
    private String status = "active"; // Event status: active, cancelled, completed
    
    private boolean hasActiveData = false; // Indicates if event has active data/assignments
}
