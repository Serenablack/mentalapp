package com.mentalapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "suggested_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "moodEntry") // prevent recursion in logs
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // safe equals/hashCode
public class SuggestedActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_entry_id", nullable = false)
    private MoodEntry moodEntry;

    @Column(name = "activity_description", columnDefinition = "TEXT", nullable = false)
    private String activityDescription;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "activity_type", length = 100)
    private String activityType;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "priority_level")
    private Integer priorityLevel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void markAsCompleted() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsIncomplete() {
        this.isCompleted = false;
        this.completedAt = null;
    }

    public String getStatus() {
        if (this.isCompleted) {
            return "completed";
        } else {
            return "pending";
        }
    }
}