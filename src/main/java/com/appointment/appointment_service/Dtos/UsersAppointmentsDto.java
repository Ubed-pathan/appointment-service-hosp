package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;

public record UsersAppointmentsDto(
        String appointmentId,
        String appointmentStatus,
        String doctorId,
        String doctorsFullName,
        String doctorSpecialization,
        LocalDateTime appointmentTime,
        String reason
) {}
