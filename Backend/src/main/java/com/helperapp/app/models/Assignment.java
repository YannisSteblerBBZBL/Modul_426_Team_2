package com.helperapp.app.models;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document(collection = "assignments")
public class Assignment {
    
    @Id
    private String id;

    @NonNull
    private String eventDay;

    @NonNull
    private Map<String, String> assignments; // Map between helper and Station: HelperID, StationID

}   
