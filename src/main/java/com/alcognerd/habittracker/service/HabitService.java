package com.alcognerd.habittracker.service;

import com.alcognerd.habittracker.dto.HabitCreate;
import com.alcognerd.habittracker.dto.HabitOut;
import com.alcognerd.habittracker.enums.HabitStatus;
import com.alcognerd.habittracker.exception.HabitHistoryNotFoundException;
import com.alcognerd.habittracker.exception.HabitNotFoundException;
import com.alcognerd.habittracker.exception.HabitStreakNotFoundException;
import com.alcognerd.habittracker.exception.NotFoundException;
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
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // Map requested days -> Set<DayOfWeek>
        Set<DayOfWeek> activeDays = habitRequest.getDays().stream()
                .map(String::toUpperCase)
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

        // Save habit
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
                .description(savedHabit.getDescription())
                .frequency(savedHabit.getFrequency())
                .habitStatus(HabitStatus.PENDING)
                .createdAt(savedHabit.getCreatedAt().toLocalDate())
                .currentStreak(0)
                .category(savedHabit.getCategory().getName())
                .build();
    }

    public List<HabitOut> getAllHabits(User user){
        List<Habit> habits = habitRepository.findByUserIdAndEnabledTrue(user.getId()); // get all active habits of the user
        List<HabitOut> habitOuts = new ArrayList<>();
        for(Habit habit :habits){
            HabitStreak habitStreak = habitStreakRepository.findByHabitId(habit.getHabitId()).orElseThrow(()->new NotFoundException("Streak not found"));

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
    public HabitOut updateTodayHabitStatus(User user, HabitStatus status, Long habitId) {

        Habit habit = habitRepository
                .findByHabitIdAndEnabledTrue(habitId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        HabitStreak habitStreak = habitStreakRepository
                .findByHabitId(habit.getHabitId())
                .orElseThrow(() -> new HabitStreakNotFoundException(habit.getHabitId()));

        HabitHistory habitHistory = habitHistoryRepository
                .findTodayHistoryByHabitId(habit.getHabitId())
                .orElseThrow(() -> new HabitHistoryNotFoundException(habit.getHabitId()));

        habitHistory.setStatus(status);

        LocalDate today = LocalDate.now();
        LocalDate previousActiveDate = getPreviousActiveDate(habit, today);

        if (status.equals(HabitStatus.COMPLETED)) {

            if (habitStreak.getLastCompletedDate() != null &&
                    habitStreak.getLastCompletedDate().equals(previousActiveDate)) {

                habitStreak.setCurrentStreak(habitStreak.getCurrentStreak() + 1);
                habitStreak.setLongestStreak(
                        Math.max(habitStreak.getCurrentStreak(), habitStreak.getLongestStreak())
                );

            } else {
                habitStreak.setCurrentStreak(1);
            }

            habitStreak.setLastCompletedDate(today);

        } else {
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

    public void disableHabit(Long habitId,Long userId){
            Habit habit = habitRepository.findByHabitIdAndUserId(habitId,userId).orElseThrow(()->new HabitNotFoundException(habitId));
        habit.setEnabled(false);
        habitRepository.save(habit);
    }


    @Transactional
    public List<HabitOut> getTodayHabits(User user) {

        ensureHabitHistoryUpToToday(user);

        LocalDate today = LocalDate.now();

        List<Habit> allHabits = habitRepository.findByUserIdAndEnabledTrue(user.getId());

        List<Habit> todayHabits = allHabits.stream()
                .filter(h -> h.isActiveOn(today))
                .toList();

        return todayHabits.stream().map(habit -> {

            HabitStreak streak = habitStreakRepository.findByHabitId(habit.getHabitId())
                    .orElseThrow(() -> new NotFoundException("Streak missing for habit " + habit.getHabitId()));

            HabitHistory todayHistory = habitHistoryRepository
                    .findByHabit_HabitIdAndCreatedAt(habit.getHabitId(), today)
                    .orElseThrow(() -> new NotFoundException("History missing for today habit " + habit.getHabitId()));

            return HabitOut.builder()
                    .id(habit.getHabitId())
                    .name(habit.getName())
                    .description(habit.getDescription())
                    .frequency(habit.getFrequency())
                    .category(habit.getCategory().getName())
                    .currentStreak(streak.getCurrentStreak())
                    .lastCompletedAt(streak.getLastCompletedDate())
                    .createdAt(habit.getCreatedAt().toLocalDate())
                    .habitStatus(todayHistory.getStatus())
                    .build();
        }).toList();
    }

    // Helper methods:
    private LocalDate getPreviousActiveDate(Habit habit, LocalDate today) {
        Set<DayOfWeek> activeDays = habit.getActiveDays();

        LocalDate date = today.minusDays(1);
        while (true) {
            if (activeDays.contains(date.getDayOfWeek())) {
                return date;
            }
            date = date.minusDays(1);
        }
    }

    public void ensureHabitHistoryUpToToday(User user) {

        LocalDate today = LocalDate.now();
        List<Habit> habits = habitRepository.findByUserIdAndEnabledTrue(user.getId());

        for (Habit habit : habits) {
            createMissingHistoryForHabit(habit, today);
        }

        createTodayHabitHistory(habits, today);  // create today habits and mark as pending
    }

    private void createMissingHistoryForHabit(Habit habit, LocalDate today) {

        LocalDate habitCreatedDate = habit.getCreatedAt().toLocalDate();

        // last saved history date
        LocalDate lastHistoryDate = habitHistoryRepository.findLastHistoryDateForHabit(habit.getHabitId());

        // if no history -> start from habit creation date - 1
        if (lastHistoryDate == null) {
            lastHistoryDate = habitCreatedDate.minusDays(1);
        }

        // fill from lastHistoryDate+1 to yesterday
        LocalDate fromDate = lastHistoryDate.plusDays(1);
        LocalDate toDate = today.minusDays(1);

        if (fromDate.isAfter(toDate)) return; // no gap to fill

        List<HabitHistory> toSave = new ArrayList<>();

        LocalDate cursor = fromDate;

        while (!cursor.isAfter(toDate)) {
            // check if habit is active on this date
            if (habit.isActiveOn(cursor)) {

                // check if this date already exists - avoid duplicates
                boolean exists = habitHistoryRepository
                        .findByHabit_HabitIdAndCreatedAt(habit.getHabitId(), cursor)
                        .isPresent();

                if (!exists) {
                    HabitHistory history = new HabitHistory();
                    history.setHabit(habit);
                    history.setCreatedAt(cursor);
                    history.setStatus(HabitStatus.MISSED);
                    toSave.add(history);
                }
            }
            cursor = cursor.plusDays(1);
        }

        if (!toSave.isEmpty()) {
            habitHistoryRepository.saveAll(toSave);
        }
    }

    private void createTodayHabitHistory(List<Habit> habits, LocalDate today) {

        List<HabitHistory> toSave = new ArrayList<>();

        for (Habit habit : habits) {

            if (!habit.isActiveOn(today)) continue;

            boolean exists = habitHistoryRepository
                    .findByHabit_HabitIdAndCreatedAt(habit.getHabitId(), today)
                    .isPresent();

            if (!exists) {
                HabitHistory history = new HabitHistory();
                history.setHabit(habit);
                history.setCreatedAt(today);
                history.setStatus(HabitStatus.PENDING);
                toSave.add(history);
            }
        }

        if (!toSave.isEmpty()) {
            habitHistoryRepository.saveAll(toSave);
        }
    }
}

