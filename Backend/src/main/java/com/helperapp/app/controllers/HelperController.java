package com.helperapp.app.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helperapp.app.models.Helper;
import com.helperapp.app.services.HelperService;

@RestController
@RequestMapping("/api/helpers")
public class HelperController {

    @Autowired
    private HelperService helperService;

    @GetMapping
    public List<Helper> getAllHelpers() {
        return helperService.getAllHelpers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Helper> getHelperById(@PathVariable String id) {
        Optional<Helper> helper = helperService.getHelperById(id);
        return helper.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Helper createHelper(@RequestBody Helper helper) {
        return helperService.createHelper(helper);
    }

    @PostMapping("/public/{eventId}")
    public ResponseEntity<Helper> createPublicHelper(@PathVariable String eventId, @RequestBody Helper helper) {
        return helperService.createPublicHelper(eventId, helper)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Helper> updateHelper(@PathVariable String id, @RequestBody Helper helperDetails) {
        Optional<Helper> updatedHelper = helperService.updateHelper(id, helperDetails);
        return updatedHelper.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHelper(@PathVariable String id) {
        if (helperService.deleteHelper(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
