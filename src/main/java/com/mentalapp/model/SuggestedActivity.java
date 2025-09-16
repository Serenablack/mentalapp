package com.mentalapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void markAsCompleted() {
        this.isCompleted = true;
    }

    public void markAsIncomplete() {
        this.isCompleted = false;
    }
}