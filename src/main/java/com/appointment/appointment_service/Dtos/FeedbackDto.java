package com.appointment.appointment_service.Dtos;

public record FeedbackDto(
        String FeedbackId,
        String appointmentId,
        String doctorId,
        String review,
        int rating
) {
}
