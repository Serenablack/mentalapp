package com.mentalapp.service.implementations;

import com.mentalapp.exception.ResourceNotFoundException;
import com.mentalapp.model.SuggestedActivity;
import com.mentalapp.model.User;
import com.mentalapp.dto.SuggestedActivityResponse;
import com.mentalapp.mapper.SuggestedActivityMapper;
import com.mentalapp.repository.SuggestedActivityRepository;
import com.mentalapp.service.SuggestedActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestedActivityServiceImpl implements SuggestedActivityService {

    private final SuggestedActivityRepository suggestedActivityRepository;
    private final SuggestedActivityMapper suggestedActivityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SuggestedActivityResponse> getActivitiesByDate(User user, Instant date) {
        // Truncate the Instant to the start of the day in UTC for a consistent date range
        Instant startOfDay = date.truncatedTo(ChronoUnit.DAYS);
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        return suggestedActivityRepository.findByUserIdAndDate(user.getId(), startOfDay, endOfDay)
                                          .stream()
                                          .map(suggestedActivityMapper::toResponse)
                                          .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuggestedActivityResponse> getTodaysActivities(User user) {
        // Use Instant.now() to get the current time
        return getActivitiesByDate(user, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public SuggestedActivityResponse getActivityById(Long activityId, User user) {
        SuggestedActivity activity = suggestedActivityRepository.findById(activityId)
                                                                .filter(a -> a.getMoodEntry().getUser().getId().equals(user.getId()))
                                                                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        return suggestedActivityMapper.toResponse(activity);
    }

    @Override
    @Transactional
    public void markAsCompleted(Long activityId, User user) {
        SuggestedActivity activity = getActivityEntity(activityId, user);

        activity.markAsCompleted();
        suggestedActivityRepository.save(activity);

        log.info("Activity {} marked as completed by user: {}", activityId, user.getEmail());
    }

    @Override
    @Transactional
    public void markAsIncomplete(Long activityId, User user) {
        SuggestedActivity activity = getActivityEntity(activityId, user);

        activity.markAsIncomplete();
        suggestedActivityRepository.save(activity);

        log.info("Activity {} marked as incomplete by user: {}", activityId, user.getEmail());
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId, User user) {
        SuggestedActivity activity = getActivityEntity(activityId, user);

        // Check if activity is completed
        if (Boolean.TRUE.equals(activity.getIsCompleted())) {
            throw new IllegalStateException("Cannot delete completed activities");
        }

        // Check if activity is from a previous day
        Instant activityDate = activity.getCreatedAt().toInstant(ZoneOffset.UTC);
        Instant startOfToday = Instant.now().truncatedTo(ChronoUnit.DAYS);

        if (activityDate.isBefore(startOfToday)) {
            throw new IllegalStateException("Cannot delete activities from previous days");
        }

        suggestedActivityRepository.delete(activity);
        log.info("Activity {} deleted by user: {}", activityId, user.getEmail());
    }

    private SuggestedActivity getActivityEntity(Long activityId, User user) {
        return suggestedActivityRepository.findById(activityId)
                                          .filter(activity -> activity.getMoodEntry().getUser().getId().equals(user.getId()))
                                          .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));
    }
}