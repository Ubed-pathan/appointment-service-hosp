package com.appointment.appointment_service.Models;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDto {

    @Id
    @Column(length = 12, nullable = false, updatable = false)
    private String feedbackId;

    @Column(nullable = false)
    private String doctorId;

    @Column(nullable = false)
    private String appointmentId;

    @Column(nullable = false)
    private int rating; // e.g., 1 to 5

    private String review;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.appointmentId == null || this.appointmentId.isEmpty()) {
            // Custom 10-character NanoID using uppercase letters and digits
            String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int size = 10;

            this.appointmentId = NanoIdUtils.randomNanoId(
                    NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                    alphabet.toCharArray(),
                    size
            );
        }
        createdAt = LocalDateTime.now();
    }
}
