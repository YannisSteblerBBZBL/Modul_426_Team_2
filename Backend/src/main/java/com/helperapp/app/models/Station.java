package com.helperapp.app.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;

@Data
@Document(collection = "stations")
public class Station {
    
    @Id
    private String id;

    @NonNull
    private String name;

    @NonNull
    private Number neededHelpers;

    @NonNull
    private Boolean is18Plus;
}
