package com.alcognerd.habittracker.controller;

import com.alcognerd.habittracker.dto.ApiResponse;
import com.alcognerd.habittracker.dto.HabitCreate;
import com.alcognerd.habittracker.dto.HabitOut;
import com.alcognerd.habittracker.dto.HabitStatusUpdateRequest;
import com.alcognerd.habittracker.enums.HabitStatus;
import com.alcognerd.habittracker.exception.NotFoundException;
import com.alcognerd.habittracker.model.Habit;
import com.alcognerd.habittracker.model.User;
import com.alcognerd.habittracker.service.HabitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    @Autowired
    private HabitService habitService;

    @PostMapping()
    public ResponseEntity<ApiResponse<HabitOut>> createHabit(@Valid @RequestBody HabitCreate habitRequest, Authentication authentication){

            User user  =(User) authentication.getPrincipal();
            if(user == null){
                throw new NotFoundException("User not found");
            }
            HabitOut habitOut = habitService.createHabit(habitRequest,user);
            ApiResponse<HabitOut> apiResponse = new ApiResponse<>(HttpStatus.CREATED.name(), "Habit added successfully",habitOut);
            return ResponseEntity.ok(apiResponse);

    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<HabitOut>>> getHabits(Authentication authentication){

            User user = (User) authentication.getPrincipal();
            List<HabitOut> habitOutList = habitService.getAllHabits(user);
            ApiResponse<List<HabitOut>> apiResponse = new ApiResponse<>(HttpStatus.OK.name(), "Fetched user habits successfully", habitOutList);
            return ResponseEntity.ok(apiResponse);

    }

    @PutMapping("/{habitId}")
    public ResponseEntity<ApiResponse<HabitOut>> updateStatus(@PathVariable Long habitId, @RequestBody HabitStatusUpdateRequest statusUpdateRequest, Authentication authentication){

            User user  =(User) authentication.getPrincipal();
            HabitOut habitOut = habitService.updateTodayHabitStatus(user,statusUpdateRequest.getStatus(),habitId);
            ApiResponse<HabitOut> apiResponse = new ApiResponse<>(HttpStatus.OK.name(), "Habit updated successfully",habitOut);
            return ResponseEntity.ok(apiResponse);

    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<HabitOut>>> getTodayHabits(Authentication authentication){
            User user  =(User) authentication.getPrincipal();

        List<HabitOut> habitOuts = habitService.getTodayHabits(user);
        ApiResponse<List<HabitOut>> apiResponse = new ApiResponse<>(HttpStatus.OK.name(), "Fetched today habits successfully",habitOuts);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{habitId}")
    public ResponseEntity<ApiResponse<?>> disableHabit(@PathVariable Long habitId,Authentication authentication){
            User user = (User) authentication.getPrincipal();
            habitService.disableHabit(habitId,user.getId());
            ApiResponse<List<HabitOut>> apiResponse = new ApiResponse<>(HttpStatus.OK.name(), "Habit deleted successfully");
            return ResponseEntity.ok(apiResponse);
    }


}
