package com.appointment.appointment_service.Dtos;

import java.time.LocalDateTime;

public record AdminFeedbackDto(
    String appointmentId,
    LocalDateTime appointmentTime,
    String userFullName,
    String userEmail,
    String doctorFullName,
    int rating,
    String review,
    String feedbackId
) {}

