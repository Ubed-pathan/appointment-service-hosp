package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;

public record AppointmentDotForAdminDashboard(
        String appointmentId,
        LocalDateTime appointmentTime,
        String AppointmentStatus,
        String userId,
        String usersFullName,
        String usersEmail,
        String doctorsFullName,
        String doctorsEmail,
        String reason,
        LocalDateTime createdAt
){
}
