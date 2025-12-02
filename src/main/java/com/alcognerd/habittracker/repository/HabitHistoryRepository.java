package com.alcognerd.habittracker.repository;

import com.alcognerd.habittracker.model.HabitHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitHistoryRepository extends JpaRepository<HabitHistory,Long> {

    @Query("""
    SELECT h FROM HabitHistory h
    WHERE h.habit.habitId = :habitId
      AND h.createdAt = CURRENT_DATE
""")
    Optional<HabitHistory> findTodayHistoryByHabitId(@Param("habitId") Long habitId);

    @Query(
            value = """
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS day,
                   COUNT(*) AS count
            FROM habit_history
            WHERE status = 'COMPLETED'
              AND created_at BETWEEN :from AND :to
            GROUP BY day
            ORDER BY day
            """,
            nativeQuery = true
    )
    List<Object[]> getCompletedInRange(LocalDate from, LocalDate to);


    @Query(
            value = """
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS day,
                   COUNT(*) AS count
            FROM habit_history
            WHERE status = 'COMPLETED'
              AND YEAR(created_at) = :year
            GROUP BY day
            ORDER BY day
            """,
            nativeQuery = true
    )
    List<Object[]> getCompletedByYear(int year);



}
