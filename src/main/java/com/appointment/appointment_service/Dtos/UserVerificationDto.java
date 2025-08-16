package com.appointment.appointment_service.Dtos;

public record UserVerificationDto(
        String userId,
        String usersFullName,
        String usersEmail
) {
}
