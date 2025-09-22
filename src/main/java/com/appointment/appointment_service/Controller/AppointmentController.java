package com.appointment.appointment_service.Controller;

import com.appointment.appointment_service.Dtos.AppointmentDto;
import com.appointment.appointment_service.Dtos.FeedbackDto;
import com.appointment.appointment_service.Models.AppointmentModel;
import com.appointment.appointment_service.Services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
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

    @GetMapping("/cancel/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(@PathVariable String appointmentId) {
        try{
            String cancelledAppointment = appointmentService.cancelAppointment(appointmentId);
            return ResponseEntity.ok(cancelledAppointment);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/complete/{appointmentId}")
    public ResponseEntity<?> completeAppointment(@PathVariable String appointmentId) {
        try {
            String completedAppointment = appointmentService.completeAppointment(appointmentId);
            return ResponseEntity.ok(completedAppointment);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/getAllAppointments" )
    public ResponseEntity<?> getAllAppointments() {
        try {
            return ResponseEntity.ok(appointmentService.getAllAppointments());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/getUserAppointments/{userId}")
    public ResponseEntity<?> getUserAppointments(@PathVariable String userId) {
        try {
            return ResponseEntity.ok(appointmentService.getUserAppointments(userId));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/getDoctorAppointments/{doctorUsername}")
    public ResponseEntity<?> getDoctorAppointments(@PathVariable String doctorUsername) {
        try {
            return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorUsername));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PatchMapping("/completeAppointment/{appointmentId}" )
    public ResponseEntity<?> completeAppointmentPatch(@PathVariable String appointmentId) {
        try {
            String completedAppointment = appointmentService.completeAppointment(appointmentId);
            return ResponseEntity.ok(completedAppointment);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PatchMapping("/cancelAppointment/{appointmentId}" )
    public ResponseEntity<?> cancelAppointmentPatch(@PathVariable String appointmentId) {
        try {
            String completedAppointment = appointmentService.cancelAppointment(appointmentId);
            return ResponseEntity.ok(completedAppointment);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/getAllPatientsOfDoctor/{doctorUsername}")
    public ResponseEntity<?> getAllPatientsOfDoctor(@PathVariable String doctorUsername) {
        try {
            return ResponseEntity.ok(appointmentService.getAllPatientsOfDoctor(doctorUsername));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/userFeedback" )
    public ResponseEntity<?> userFeedback(@RequestBody @Valid FeedbackDto dto) {

        try {
            appointmentService.userFeedback(dto);
            return ResponseEntity.ok("Feedback submitted successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }

    @GetMapping("/getFeedbacksForAdmin/{doctorId}")
    public ResponseEntity<?> getFeedbacksForAdmin(@PathVariable String doctorId) {
        try {
            var feedbacks = appointmentService.getFeedbacksForAdmin(doctorId);
            return ResponseEntity.ok(feedbacks);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


    @DeleteMapping("/deleteFeedback/{feedbackId}")
    public ResponseEntity<?> deleteFeedback(@PathVariable String feedbackId) {
        try {
            appointmentService.deleteFeedback(feedbackId);
            return ResponseEntity.ok("Feedback deleted successfully.");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/doctorsBookedAppointments/{doctorUsername}" )
    public ResponseEntity<?> doctorsBookedAppointments(@PathVariable String doctorUsername) {
        try {
            var appointments = appointmentService.doctorsBookedAppointments(doctorUsername);
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}
