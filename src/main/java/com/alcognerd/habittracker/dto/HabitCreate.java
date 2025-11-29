package com.alcognerd.habittracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HabitCreate {
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @NotBlank(message = "Frequency is required")
//    @Pattern(
//            regexp = "DAILY|WEEKLY|MONTHLY",
//            message = "Frequency must be one of: DAILY, WEEKLY, MONTHLY"
//    )
    private String frequency;
}
