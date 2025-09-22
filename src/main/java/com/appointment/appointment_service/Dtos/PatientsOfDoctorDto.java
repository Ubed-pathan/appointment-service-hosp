package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;

public record PatientsOfDoctorDto(
        String appointmentId,
        LocalDateTime appointmentStartTime,
        String appointmentStatus,
        String userId,
        String usersFullName,
        String usersEmail,
        String reason
) {
}
