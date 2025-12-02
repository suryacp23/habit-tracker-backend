package com.alcognerd.habittracker.service;

import com.alcognerd.habittracker.repository.HabitHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HabitHistoryService {

    private final HabitHistoryRepository repo;

    private Map<String, Long> convert(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        return map;
    }


    public Optional<Map<String, Long>> getCompletedCounts(String year) {

        if (year.equalsIgnoreCase("current")) {
            LocalDate today = LocalDate.now();
            LocalDate from = today.minusDays(365);

            List<Object[]> rows = repo.getCompletedInRange(from, today);
            Map<String, Long> data = convert(rows);

            return data.isEmpty() ? Optional.empty() : Optional.of(data);
        }

        int y = Integer.parseInt(year);
        List<Object[]> rows = repo.getCompletedByYear(y);
        Map<String, Long> data = convert(rows);
        return data.isEmpty() ? Optional.empty() : Optional.of(data);
    }
}