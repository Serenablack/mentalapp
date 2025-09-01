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

import java.time.LocalDateTime;
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
    public List<SuggestedActivityResponse> getActivitiesByDate(User user, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return suggestedActivityRepository.findByUserIdAndDate(user.getId(), startOfDay, endOfDay)
                .stream()
                .map(suggestedActivityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuggestedActivityResponse> getTodaysActivities(User user) {
        return getActivitiesByDate(user, LocalDateTime.now());
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
        if (activity.getIsCompleted()) {
            throw new IllegalStateException("Cannot delete completed activities");
        }

        // Check if activity is from previous days (can only delete same day or future
        // activities)
        LocalDateTime activityDate = activity.getMoodEntry().getCreatedAt();
        LocalDateTime today = LocalDateTime.now();

        if (activityDate.toLocalDate().isBefore(today.toLocalDate())) {
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