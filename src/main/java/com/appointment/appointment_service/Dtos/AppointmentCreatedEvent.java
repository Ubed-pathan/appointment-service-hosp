package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;

public record AppointmentCreatedEvent(
        String appointmentId,
        String userId,
        String userName,
        String userEmail,
        String doctorId,
        LocalDateTime appointmentTime,
        String reason
) {}

