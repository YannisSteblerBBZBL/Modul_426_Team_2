package com.helperapp.app.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Helper;
import com.helperapp.app.repositories.HelperRepository;

@Service
public class HelperService {

    @Autowired
    private HelperRepository helperRepository;

    public List<Helper> getAllHelpers() {
        return helperRepository.findAll();
    }

    public Optional<Helper> getHelperById(String id) {
        return helperRepository.findById(id);
    }

    public Helper createHelper(Helper helper) {
        helper.setAge(calculateAge(helper.getBirthdate()));
        return helperRepository.save(helper);
    }

    public Optional<Helper> updateHelper(String id, Helper updatedHelper) {
        return helperRepository.findById(id).map(helper -> {
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
        if (helperRepository.existsById(id)) {
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
