package com.helperapp.app.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Helper;
import com.helperapp.app.repositories.HelperRepository;
import com.helperapp.app.security.JwtHelper;

@Service
public class HelperService {

    @Autowired
    private HelperRepository helperRepository;

    @Autowired
    private JwtHelper jwtHelper;

    public List<Helper> getAllHelpers() {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return helperRepository.findAll().stream()
                .filter(helper -> helper.getUserId().equals(currentUserId))
                .collect(Collectors.toList());
    }

    public Optional<Helper> getHelperById(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        Optional<Helper> helper = helperRepository.findById(id);
        return helper.filter(h -> h.getUserId().equals(currentUserId));
    }

    public Helper createHelper(Helper helper) {
        helper.setUserId(jwtHelper.getUserIdFromToken());
        helper.setAge(calculateAge(helper.getBirthdate()));
        return helperRepository.save(helper);
    }

    public Optional<Helper> updateHelper(String id, Helper updatedHelper) {
        String currentUserId = jwtHelper.getUserIdFromToken();

        return helperRepository.findById(id).filter(h -> h.getUserId().equals(currentUserId)).map(helper -> {
            helper.setFirstname(updatedHelper.getFirstname());
            helper.setLastname(updatedHelper.getLastname());
            helper.setEmail(updatedHelper.getEmail());
            helper.setBirthdate(updatedHelper.getBirthdate());
            helper.setAge(calculateAge(updatedHelper.getBirthdate()));
            helper.setPresence(updatedHelper.getPresence());
            helper.setPreferences(updatedHelper.getPreferences());
            helper.setPreferencedHelpers(updatedHelper.getPreferencedHelpers());
            helper.setPreferencedStations(updatedHelper.getPreferencedStations());
            return helperRepository.save(helper);
        });
    }

    public boolean deleteHelper(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        Optional<Helper> helper = helperRepository.findById(id);

        if (helper.isPresent() && helper.get().getUserId().equals(currentUserId)) {
            helperRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private String calculateAge(LocalDate birthdate) {
        if (birthdate == null) return "Unknown";
        return String.valueOf(Period.between(birthdate, LocalDate.now()).getYears());
    }
}
