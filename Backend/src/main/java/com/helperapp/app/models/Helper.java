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

    @NonNull
    private String firstname;

    @NonNull
    private String lastname;

    private String email;

    @NonNull
    private LocalDate birthdate;

    private String age;

    private List<Number> presence; // List of presence EventDays (i.g. 1, 4, 5)

    private List<String> preferences;

    private List<String> preferencedHelpers;

    private List<String> preferencedStations;

}
