package com.alcognerd.habittracker.model;

import com.alcognerd.habittracker.enums.HabitStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "habit_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HabitHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "habit_history_id")
    private Long habitHistoryId;

    private LocalDateTime completedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private HabitStatus status;

    @ManyToOne
    @JoinColumn(name = "habit_id", referencedColumnName = "habit_id", nullable = false)
    private Habit habit;

}
