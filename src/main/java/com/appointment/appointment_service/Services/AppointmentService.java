package com.appointment.appointment_service.Services;

import com.appointment.appointment_service.Clients.DoctorClient;
import com.appointment.appointment_service.Clients.UserClient;
import com.appointment.appointment_service.Dtos.AppointmentCreatedEvent;
import com.appointment.appointment_service.Dtos.AppointmentDto;
import com.appointment.appointment_service.Dtos.UserVerificationDto;
import com.appointment.appointment_service.Models.AppointmentModel;
import com.appointment.appointment_service.Repositories.AppointmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorClient doctorServiceClient;
    private final UserClient userServiceClient;
    private final KafkaTemplate<String, AppointmentCreatedEvent> kafkaTemplate;

    public String bookAppointment(AppointmentDto dto) {

        UserVerificationDto userDto = new UserVerificationDto(dto.userId(), dto.usersFullName(), dto.usersEmail());

        boolean isUserValid = userServiceClient.isValidUser(userDto);
        if( !isUserValid ) {
            throw new RuntimeException("User does not exist or is invalid.");
        }
        // Only checking doctor existence now — user data comes from frontend
        String doctorFullName = doctorServiceClient.isDocterExists(dto.doctorId());
        if (doctorFullName.isEmpty()) throw new RuntimeException("Doctor does not exist.");

        LocalDate appointmentDate = dto.appointmentTime().toLocalDate();
        LocalDateTime startOfDay = appointmentDate.atStartOfDay();
        LocalDateTime endOfDay = appointmentDate.atTime(23, 59, 59);

        boolean alreadyBooked = appointmentRepository.existsByUserIdAndDoctorIdAndAppointmentTimeBetween(
                dto.userId(), dto.doctorId(), startOfDay, endOfDay);
        if (alreadyBooked) throw new RuntimeException("User already has an appointment with this doctor on the same day.");

        boolean slotTaken = appointmentRepository.existsByDoctorIdAndAppointmentTime(
                dto.doctorId(), dto.appointmentTime());
        if (slotTaken) throw new RuntimeException("Appointment slot is already booked.");

        // Save appointment
        AppointmentModel appointmentModel = new AppointmentModel();
        appointmentModel.setAppointmentTime(dto.appointmentTime());
        appointmentModel.setUserId(dto.userId());
        appointmentModel.setDoctorId(dto.doctorId());
        appointmentModel.setReason(dto.reason());
        appointmentRepository.save(appointmentModel);

        // Build event directly from dto
        var event = new AppointmentCreatedEvent(
                appointmentModel.getAppointmentId(),
                dto.userId(),
                dto.usersFullName(),
                dto.usersEmail(),
                doctorFullName,
                dto.appointmentTime(),
                dto.reason()
        );

        // Publish to Kafka
        kafkaTemplate.send("appointments.created", appointmentModel.getAppointmentId(), event)
                .thenAccept(result ->
                        System.out.println("Published appointment-created event: " + appointmentModel.getAppointmentId())
                )
                .exceptionally(ex -> {
                    System.err.println("Failed to publish appointment event: " + ex.getMessage());
                    // Optional: store in an outbox for retry
                    return null;
                });


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        String formattedTime = appointmentModel.getAppointmentTime().format(formatter);
        return "Appointment booked at " + formattedTime +
                ". Your appointment ID is " + appointmentModel.getAppointmentId();
    }

    public String cancelAppointment(String appointmentId) {
        AppointmentModel appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (appointment.getStatus() == AppointmentModel.AppointmentStatus.CANCELLED) {
            return "Appointment is already cancelled.";
        }

        appointment.setStatus(AppointmentModel.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // Publish cancellation event
//        kafkaTemplate.send("appointments.cancelled", appointmentId, appointment)
//                .thenAccept(result ->
//                        System.out.println("Published appointment-cancelled event: " + appointmentId)
//                )
//                .exceptionally(ex -> {
//                    System.err.println("Failed to publish cancellation event: " + ex.getMessage());
//                    return null;
//                });

        return "Appointment with ID " + appointmentId + " has been cancelled.";
    }

    public String completeAppointment(String appointmentId) {
        AppointmentModel appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (appointment.getStatus() == AppointmentModel.AppointmentStatus.COMPLETED) {
            return "Appointment is already completed.";
        }

        appointment.setStatus(AppointmentModel.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

//        // Publish completion event
////        kafkaTemplate.send("appointments.completed", appointmentId, appointment)
//                .thenAccept(result ->
//                        System.out.println("Published appointment-completed event: " + appointmentId)
//                )
//                .exceptionally(ex -> {
//                    System.err.println("Failed to publish completion event: " + ex.getMessage());
//                    return null;
//                });

        return "Appointment with ID " + appointmentId + " has been completed.";
    }
}
