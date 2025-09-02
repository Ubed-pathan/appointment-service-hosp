package com.appointment.appointment_service.Dtos;

public record AppointmentDotForAdminDashboard(
        String appointmentId,
        String appointmentTime,
        String status,
        String userId,
        String usersFullName,
        String usersEmail,
        String usersPhoneNumber,
        String doctorId,
        String doctorsFullName,
        String doctorsEmail,
        String doctorsPhoneNumber
){
}
