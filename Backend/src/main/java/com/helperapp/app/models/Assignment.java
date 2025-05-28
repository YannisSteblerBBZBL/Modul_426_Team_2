package com.helperapp.app.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "assignments")
public class Assignment {
    
    @Id
    private String id;

    private String userId;

    @NonNull
    private String eventId;

    @NonNull
    private String eventDay;

    @NonNull
    private String helperId;

    @NonNull
    private String stationId;

}   
