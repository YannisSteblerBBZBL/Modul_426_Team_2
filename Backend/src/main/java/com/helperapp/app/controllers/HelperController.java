package com.helperapp.app.controllers;

import java.util.List;

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
    public ResponseEntity<Helper> getHelperById(@PathVariable Long id) {
        return helperService.getHelperById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Helper createHelper(@RequestBody Helper helper) {
        return helperService.createHelper(helper);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Helper> updateHelper(@PathVariable Long id, @RequestBody Helper helper) {
        return helperService.updateHelper(id, helper)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHelper(@PathVariable Long id) {
        return helperService.deleteHelper(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
