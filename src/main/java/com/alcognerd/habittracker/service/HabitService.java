package com.alcognerd.habittracker.service;

import com.alcognerd.habittracker.dto.HabitCreate;
import com.alcognerd.habittracker.dto.HabitOut;
import com.alcognerd.habittracker.enums.HabitStatus;
import com.alcognerd.habittracker.exception.HabitHistoryNotFoundException;
import com.alcognerd.habittracker.exception.HabitNotFoundException;
import com.alcognerd.habittracker.exception.HabitStreakNotFoundException;
import com.alcognerd.habittracker.model.*;
import com.alcognerd.habittracker.repository.CategoryRepository;
import com.alcognerd.habittracker.repository.HabitHistoryRepository;
import com.alcognerd.habittracker.repository.HabitRepository;
import com.alcognerd.habittracker.repository.HabitStreakRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HabitService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private HabitStreakRepository habitStreakRepository;
    @Autowired
    private HabitHistoryRepository habitHistoryRepository;

    public HabitOut createHabit(HabitCreate habitRequest, User user) {

        // Find category
        Category category = categoryRepository.findByName(habitRequest.getCategory())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Map requested days -> Set<DayOfWeek>
        Set<DayOfWeek> activeDays = habitRequest.getDays().stream()
                .map(String::toUpperCase)             // just in case
                .map(DayOfWeek::valueOf)              // "SUNDAY" -> DayOfWeek.SUNDAY
                .collect(Collectors.toSet());

        // Optional: validate DAILY means all 7 days, WEEKLY at least 1
        if ("DAILY".equalsIgnoreCase(habitRequest.getFrequency())
                && activeDays.size() != 7) {
            throw new IllegalArgumentException("DAILY habits must be active on all 7 days");
        }

        // Build Habit entity
        Habit habit = Habit.builder()
                .name(habitRequest.getName())
                .user(user)
                .description(habitRequest.getDescription())
                .frequency(habitRequest.getFrequency())
                .category(category)
                .activeDays(activeDays)
                .enabled(true)
                .build();

        // Save habit first
        Habit savedHabit = habitRepository.save(habit);

        // Create HabitStreak for this habit
        HabitStreak habitStreak = HabitStreak.builder()
                .habit(savedHabit)
                .currentStreak(0)
                .longestStreak(0)
                .lastCompletedDate(null)
                .build();
        HabitStreak savedHabitStreak = habitStreakRepository.save(habitStreak);

        // Create HabitHistory for today only if habit is active today
        LocalDate today = LocalDate.now();
        if (savedHabit.isActiveOn(today)) {
            HabitHistory habitHistory = new HabitHistory(savedHabit);
            habitHistoryRepository.save(habitHistory);
        }

        // Build output DTO
        return HabitOut.builder()
                .id(savedHabit.getHabitId())
                .name(savedHabit.getName())
                .description(savedHabit.getDescription())       // fixed
                .frequency(savedHabit.getFrequency())
                .habitStatus(HabitStatus.PENDING)
                .createdAt(savedHabit.getCreatedAt().toLocalDate())
                .currentStreak(0)
                .category(savedHabit.getCategory().getName())
                .build();
    }

    public List<HabitOut> getAllHabits(User user){
        List<Habit> habits = habitRepository.findByUserId(user.getId());
        List<HabitOut> habitOuts = new ArrayList<>();
        for(Habit habit :habits){
            HabitStreak habitStreak = habitStreakRepository.findByHabitId(habit.getHabitId()).orElseThrow(()->new RuntimeException("Streak not found"));

            HabitOut habitOut = HabitOut.builder()
                    .id(habit.getHabitId())
                    .name(habit.getName())
                    .description((habit.getDescription()))
                    .frequency(habit.getFrequency())
                    .createdAt(habit.getCreatedAt().toLocalDate())
                    .category(habit.getCategory().getName())
                    .currentStreak(habitStreak.getCurrentStreak())
                    .lastCompletedAt(habitStreak.getLastCompletedDate())
                    .build();
            System.out.println(habitOut.toString());
            habitOuts.add(habitOut);
        }
        return habitOuts;
    }

    @Transactional
    public HabitOut updateTodayHabitStatus(User user,HabitStatus status,Long habitId){
        Habit habit = habitRepository.findByHabitIdAndEnabledTrue(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));

        // streak must exist
        HabitStreak habitStreak = habitStreakRepository.findByHabitId(habit.getHabitId()).orElseThrow(() -> new HabitStreakNotFoundException(habit.getHabitId()));
        // today's history must exist
        HabitHistory habitHistory = habitHistoryRepository.findTodayHistoryByHabitId(habit.getHabitId()).orElseThrow(() -> new HabitHistoryNotFoundException(habit.getHabitId()));
        habitHistory.setStatus(status);

        if(status.equals(HabitStatus.COMPLETED)){
            if (habitStreak.getLastCompletedDate() != null &&
                    habitStreak.getLastCompletedDate().equals(LocalDate.now().minusDays(1))) {
                habitStreak.setCurrentStreak(habitStreak.getCurrentStreak() + 1);
                habitStreak.setLongestStreak(
                        Math.max(habitStreak.getCurrentStreak(), habitStreak.getLongestStreak())
                );
            } else {
                habitStreak.setCurrentStreak(1); // first completion in a new streak
            }
            habitStreak.setLastCompletedDate(LocalDate.now());

        }else {
            habitStreak.setCurrentStreak(0);
        }
        habitHistoryRepository.save(habitHistory);
        habitStreakRepository.save(habitStreak);
        return HabitOut.builder()
                .id(habit.getHabitId())
                .name(habit.getName())
                .description(habit.getDescription())
                .frequency(habit.getFrequency())
                .createdAt(habit.getCreatedAt().toLocalDate())
                .habitStatus(habitHistory.getStatus())
                .category(habit.getCategory().getName())
                .currentStreak(habitStreak.getCurrentStreak())
                .lastCompletedAt(habitStreak.getLastCompletedDate())
                .build();
    }


    public List<HabitOut> getTodayHabits(User user){
        LocalDate today = LocalDate.now();
        List<Habit> allHabits = habitRepository.findByUserIdAndEnabledTrue(user.getId());

        List<Habit> todayHabits= allHabits.stream()
                .filter(h -> h.isActiveOn(today))
                .toList();

        List<HabitOut> habitOuts = todayHabits.stream()
                .map(habit -> {
                    HabitStreak habitStreak = habitStreakRepository.findByHabitId(habit.getHabitId()).orElseThrow(()->new RuntimeException("Streak not found for habit: " + habit.getHabitId()));
                    HabitHistory habitHistory = habitHistoryRepository.findTodayHistoryByHabitId(habit.getHabitId()).orElseThrow(()->new RuntimeException("Habit history not found for this habit id: "+habit.getHabitId()));
                    return HabitOut.builder()
                            .name(habit.getName())
                            .description(habit.getDescription())
                            .createdAt(habit.getCreatedAt().toLocalDate())
                            .lastCompletedAt(habitStreak.getLastCompletedDate())
                            .currentStreak(habitStreak.getCurrentStreak())
                            .id(habit.getHabitId())
                            .category(habit.getCategory().getName())
                            .frequency(habit.getFrequency())
                            .habitStatus(habitHistory.getStatus())
                            .build();
                }).toList();
        return habitOuts;
    }

    public void disableHabit(Long habitId,Long userId){
            Habit habit = habitRepository.findByHabitIdAndUserId(habitId,userId).orElseThrow(()->new HabitNotFoundException(habitId));
        habit.setEnabled(false);
        habitRepository.save(habit);
    }
}

