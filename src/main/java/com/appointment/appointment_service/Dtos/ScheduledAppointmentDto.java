package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;

public record ScheduledAppointmentDto(
        String appointmentId,
        LocalDateTime appointmentStartTime,
        LocalDateTime appointmentEndTime
) {}
