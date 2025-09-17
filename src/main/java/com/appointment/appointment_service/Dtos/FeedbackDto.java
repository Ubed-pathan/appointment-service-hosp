package com.appointment.appointment_service.Dtos;

public record FeedbackDto(
        String appointmentId,
        String doctorId,
        String review,
        int rating
) {
}
