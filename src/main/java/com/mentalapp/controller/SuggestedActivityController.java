package com.mentalapp.controller;

import com.mentalapp.model.User;
import com.mentalapp.dto.SuggestedActivityResponse;
import com.mentalapp.service.SuggestedActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Slf4j
public class SuggestedActivityController {

    private final SuggestedActivityService suggestedActivityService;

    @GetMapping
    public ResponseEntity<List<SuggestedActivityResponse>> getActivitiesByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX") Instant date,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(suggestedActivityService.getActivitiesByDate(user, date));
    }

    @GetMapping("/today")
    public ResponseEntity<List<SuggestedActivityResponse>> getTodaysActivities(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(suggestedActivityService.getTodaysActivities(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuggestedActivityResponse> getActivityById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(suggestedActivityService.getActivityById(id, user));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> markAsCompleted(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        suggestedActivityService.markAsCompleted(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/incomplete")
    public ResponseEntity<Void> markAsIncomplete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        suggestedActivityService.markAsIncomplete(id, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        suggestedActivityService.deleteActivity(id, user);
        return ResponseEntity.ok().build();
    }
}
