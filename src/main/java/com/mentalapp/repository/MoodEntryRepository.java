package com.mentalapp.repository;

import com.mentalapp.model.MoodEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MoodEntryRepository extends JpaRepository<MoodEntry, Long> {

        @Query("SELECT m FROM MoodEntry m WHERE m.user.id = :userId AND m.createdAt >= :startOfDay AND m.createdAt < :endOfDay")
        List<MoodEntry> findByUserIdAndDate(@Param("userId") Long userId, @Param("startOfDay") Instant startOfDay,
                        @Param("endOfDay") Instant endOfDay);

        @Query("SELECT COUNT(m) FROM MoodEntry m WHERE m.user.id = :userId AND m.createdAt >= :startOfDay AND m.createdAt < :endOfDay")
        long countTodayEntriesByUserId(@Param("userId") Long userId, @Param("startOfDay") Instant startOfDay,
                        @Param("endOfDay") Instant endOfDay);

        @Query("SELECT m FROM MoodEntry m WHERE m.user.id = :userId AND m.createdAt >= :startDate AND m.createdAt < :endDate")
        List<MoodEntry> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);
}