package com.alcognerd.habittracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "habit_streaks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HabitStreak {

    @Id
    @Column(name = "habit_id")
    private Long habitId;   // same as Habit.id

    @OneToOne
    @MapsId
    @JoinColumn(name = "habit_id")
    private Habit habit;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak = 0;

    @Column(name = "last_completed_date")
    private LocalDate lastCompletedDate;
}
