package com.helperapp.app.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.helperapp.app.models.Event;
import com.helperapp.app.models.Helper;
import com.helperapp.app.repositories.EventRepository;
import com.helperapp.app.repositories.HelperRepository;
import com.helperapp.app.security.JwtHelper;

@Service
public class HelperService {

    @Autowired
    private HelperRepository helperRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtHelper jwtHelper;

    public List<Helper> getAllHelpers() {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return helperRepository.findAll().stream()
                .filter(h -> h.getUserId().equals(currentUserId))
                .collect(Collectors.toList());
    }

    public Optional<Helper> getHelperById(String id) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        return helperRepository.findById(id)
                .filter(h -> h.getUserId().equals(currentUserId));
    }

    public Helper createHelper(Helper helper) {
        String currentUserId = jwtHelper.getUserIdFromToken();
        helper.setUserId(currentUserId);
        helper.setAge(calculateAge(helper.getBirthdate()));
        return helperRepository.save(helper);
    }

    public Optional<Helper> createPublicHelper(String eventId, Helper helper) {
        return eventRepository.findById(eventId)
                .filter(Event::isHelperRegistrationOpen)
                .map(event -> {
                    helper.setUserId(event.getUserId());
                    return helperRepository.save(helper);
                });
    }

    public Optional<Helper> updateHelper(String id, Helper helperDetails) {
        String currentUserId = jwtHelper.getUserIdFromToken();

        return helperRepository.findById(id)
                .filter(h -> h.getUserId().equals(currentUserId))
                .map(helper -> {
                    helper.setFirstname(helperDetails.getFirstname());
                    helper.setLastname(helperDetails.getLastname());
                    helper.setEmail(helperDetails.getEmail());
                    helper.setBirthdate(helperDetails.getBirthdate());
                    helper.setAge(calculateAge(helperDetails.getBirthdate()));
                    helper.setPresence(helperDetails.getPresence());
                    helper.setPreferences(helperDetails.getPreferences());
                    helper.setPreferencedHelpers(helperDetails.getPreferencedHelpers());
                    helper.setPreferencedStations(helperDetails.getPreferencedStations());
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
