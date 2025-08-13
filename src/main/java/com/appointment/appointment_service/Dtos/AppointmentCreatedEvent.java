package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;

public record AppointmentCreatedEvent(
        String appointmentId,
        String userId,
        String usersFullName,
        String userEmail,
        String doctorFullName,
        LocalDateTime appointmentTime,
        String reason
) {}

