package com.appointment.appointment_service.Models;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "appointment")
@EqualsAndHashCode(exclude = "appointment")
public class FeedbackModel {

    @Column(nullable = false)
    private String doctorId;

    @Id
    @Column(length = 12, nullable = false, updatable = false)
    private String feedbackId;

    @Column(nullable = false)
    private int rating; // e.g., 1 to 5

    private String review;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", referencedColumnName = "appointmentId", nullable = false)
    private AppointmentModel appointment;

    @PrePersist
    protected void onCreate() {
        if (this.feedbackId == null || this.feedbackId.isEmpty()) {
            // Custom 10-character NanoID using uppercase letters and digits
            String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            int size = 10;

            this.feedbackId = NanoIdUtils.randomNanoId(
                    NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                    alphabet.toCharArray(),
                    size
            );
        }
        createdAt = LocalDateTime.now();
    }
}
