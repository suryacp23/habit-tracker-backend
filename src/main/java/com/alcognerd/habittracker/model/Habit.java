    package com.alcognerd.habittracker.model;

    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.PositiveOrZero;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.time.LocalDateTime;

    @Entity
    @Table(name = "habits")
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class Habit {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "habit_id")
        private Long id;

        @Column(nullable = false)
        private String name;

        @ManyToOne
        @JoinColumn(name = "user_id",referencedColumnName = "id")
        private User user;

        private String description;

        private String frequency;

        @ManyToOne
        @JoinColumn(name = "category_id",referencedColumnName = "category_id")
        private Category category;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        @NotNull
        @PositiveOrZero
        private Integer streak = 0;

        @NotNull
        @PositiveOrZero
        private Integer maxStreak = 0;

        @PrePersist
        protected void onCreate() {
            LocalDateTime now = LocalDateTime.now();
            this.createdAt = now;
            this.updatedAt = now;
        }

        @PreUpdate
        private void validateStreaks() {
            if (maxStreak < streak) {
                throw new IllegalArgumentException("maxStreak must be greater than or equal to streak");
            }
            this.updatedAt = LocalDateTime.now();
        }
    }
