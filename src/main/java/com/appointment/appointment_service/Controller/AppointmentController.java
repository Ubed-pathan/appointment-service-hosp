package com.appointment.appointment_service.Controller;

import com.appointment.appointment_service.Dtos.AppointmentDto;
import com.appointment.appointment_service.Models.AppointmentModel;
import com.appointment.appointment_service.Services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody @Valid AppointmentDto dto) {
        try {
            String bookedAppointment = appointmentService.bookAppointment(dto);
            return ResponseEntity.ok(bookedAppointment);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
