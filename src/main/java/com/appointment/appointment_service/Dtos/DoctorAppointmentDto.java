package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;
import java.util.List;

public record DoctorAppointmentDto(
        String appointmentId,
        String appointmentStatus,
        String userId,
        String usersFullName,
        String userEmail,
        String reason,
        LocalDateTime appointmentTime,
        boolean didUserGiveFeedback,
        List<FeedbackDto> feedbacks
) {
}
