package com.alcognerd.habittracker.controller;

import com.alcognerd.habittracker.dto.ApiResponse;
import com.alcognerd.habittracker.dto.HabitCreate;
import com.alcognerd.habittracker.model.Habit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/habits")
public class HabitController {

    public ResponseEntity<ApiResponse<Habit>> createHabit(@RequestBody HabitCreate habitRequest){

    }

}
