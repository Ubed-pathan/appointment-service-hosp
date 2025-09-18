package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;
import java.util.List;

public record UsersAppointmentsDto(
        String appointmentId,
        String appointmentStatus,
        String doctorId,
        String doctorsFullName,
        String doctorSpecialization,
        LocalDateTime appointmentTime,
        String reason,
        boolean didUserGiveFeedback,
        List<FeedbackDto> feedbacks
) {}
