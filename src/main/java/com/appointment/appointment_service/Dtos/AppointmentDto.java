package com.appointment.appointment_service.Dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentDto(

        @NotNull(message = "Appointment time cannot be blank")
        LocalDateTime appointmentTime,

        @NotBlank(message = "userId ID cannot be blank")
        String userId,

        @NotBlank(message = "Doctor ID cannot be blank")
        String doctorId,

        @NotBlank(message = "Reason for appointment cannot be blank")
        @Size(max = 250, message = "Reason cannot exceed 250 characters")
        String reason
){
}
