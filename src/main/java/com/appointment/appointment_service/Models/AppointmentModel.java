package com.appointment.appointment_service.Models;

import java.time.LocalDateTime;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_appointments_doctor_start_time",
                        columnNames = {"doctor_id", "appointment_start_time"}
                )
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "feedbacks")
@EqualsAndHashCode(exclude = "feedbacks")
public class AppointmentModel {

    @Id
    @Column(length = 12, nullable = false, updatable = false)
    private String appointmentId;

    @Column(
            name = "appointment_start_time",
            nullable = false,
            updatable = false
    )
    private LocalDateTime appointmentStartTime; // renamed from appointmentTime

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime appointmentEndTime; // unchanged

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
            name = "doctor_id",
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
            length = 100
    )
    private String doctorUsername;

    @Column(
            nullable = false
    )
    private String doctorSpecialization;

    private String appointmentType;

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

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean didUserGiveFeedback = false;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<FeedbackModel> feedbacks;

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
