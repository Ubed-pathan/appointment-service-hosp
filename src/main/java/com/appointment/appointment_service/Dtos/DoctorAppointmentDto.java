package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;

public record DoctorAppointmentDto(
        String appointmentId,
        String appointmentStatus,
        String userId,
        String usersFullName,
        String userEmail,
        String reason,
        LocalDateTime appointmentTime
) {
}
