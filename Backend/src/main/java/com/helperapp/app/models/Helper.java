package com.helperapp.app.models;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document(collection = "helpers")
public class Helper {

    @Id
    private String id;

    private String userId;

    @NonNull
    private String firstname;

    @NonNull
    private String lastname;

    private String email;

    @NonNull
    private LocalDate birthdate;

    private String age;

    private String eventId;

    private List<Number> presence; 

    private List<String> preferences;

    private List<String> preferencedHelpers;

    private List<String> preferencedStations;

}
