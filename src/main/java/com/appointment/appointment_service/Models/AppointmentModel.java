package com.appointment.appointment_service.Models;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentModel {

    @Id
    @Column(length = 12, nullable = false, updatable = false)
    private String appointmentId;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime appointmentTime;

    @Column(
            nullable = false,
            updatable = false
    )
    private String userId;

    @Column(
            nullable = false,
            length = 100
    )
    private String usersFullName;

    @Column(
            nullable = false,
            length = 100
    )
    private String usersEmail;

    @Column(
            nullable = false,
            updatable = false
    )
    private String doctorId;

    @Column(
            nullable = false,
            length = 100
    )
    private String doctorFullName;

    @Column(
                    nullable = false,
                    length = 250
    )
    private String reason;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    public enum AppointmentStatus {
        SCHEDULED,
        CANCELLED,
        COMPLETED,
    }

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

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
