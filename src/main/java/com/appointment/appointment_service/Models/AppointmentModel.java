package com.appointment.appointment_service.Models;

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
    @GeneratedValue(strategy = GenerationType.UUID)
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
            updatable = false
    )
    private String doctorId;

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
        createdAt = LocalDateTime.now();
    }
}
