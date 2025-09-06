package com.appointment.appointment_service.Services;

import com.appointment.appointment_service.Clients.DoctorClient;
import com.appointment.appointment_service.Clients.UserClient;
import com.appointment.appointment_service.Dtos.*;
import com.appointment.appointment_service.Models.AppointmentModel;
import com.appointment.appointment_service.Repositories.AppointmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorClient doctorServiceClient;
    private final UserClient userServiceClient;
    private final KafkaTemplate<String, AppointmentCreatedEvent> kafkaTemplate;

    private UserVerificationDto checkUserValidity(UserVerificationDto userDto) {
        return userServiceClient.isValidUser(userDto);
    }

    public String bookAppointment(AppointmentDto dto) {

        UserVerificationDto userDto = new UserVerificationDto(dto.userId(), dto.usersFullName(), dto.usersEmail());

        UserVerificationDto isUserValid = checkUserValidity(userDto);
        if(isUserValid == null) {
            throw new RuntimeException("User does not exist or is invalid.");
        }
        // Only checking doctor existence now — user data comes from frontend
        DoctorDto doctorDto = doctorServiceClient.isDocterExists(dto.doctorId());
        if (doctorDto == null) throw new RuntimeException("Doctor does not exist.");

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
        appointmentModel.setUsersEmail(isUserValid.usersEmail());
        appointmentModel.setUsersFullName(isUserValid.usersFullName());
        appointmentModel.setDoctorId(dto.doctorId());
        appointmentModel.setDoctorFullName(doctorDto.doctorsFullName());
        appointmentModel.setDoctorUsername(doctorDto.doctorUsername());
        appointmentModel.setDoctorSpecialization(doctorDto.doctorSpecialization());
        appointmentModel.setReason(dto.reason());
        appointmentRepository.save(appointmentModel);

        // Build event directly from dto
        var event = new AppointmentCreatedEvent(
                appointmentModel.getAppointmentId(),
                dto.userId(),
                dto.usersFullName(),
                dto.usersEmail(),
                doctorDto.doctorsFullName(),
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

        // Build event directly from dto
        var event = new AppointmentCreatedEvent(
                appointment.getAppointmentId(),
                appointment.getUserId(),
                appointment.getUsersFullName(),
                appointment.getUsersEmail(),
                appointment.getDoctorFullName(),
                appointment.getAppointmentTime(),
                appointment.getReason()
        );

        // Publish cancellation event
        kafkaTemplate.send("appointments.cancelled", appointmentId, event)
                .thenAccept(result ->
                        System.out.println("Published appointment-cancelled event: " + appointmentId)
                )
                .exceptionally(ex -> {
                    System.err.println("Failed to publish cancellation event: " + ex.getMessage());
                    return null;
                });

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

        // Build event directly from dto
        var event = new AppointmentCreatedEvent(
                appointment.getAppointmentId(),
                appointment.getUserId(),
                appointment.getUsersFullName(),
                appointment.getUsersEmail(),
                appointment.getDoctorFullName(),
                appointment.getAppointmentTime(),
                appointment.getReason()
        );

        // Publish completion event
        kafkaTemplate.send("appointments.completed", appointmentId, event)
                .thenAccept(result ->
                        System.out.println("Published appointment-completed event: " + appointmentId)
                )
                .exceptionally(ex -> {
                    System.err.println("Failed to publish completion event: " + ex.getMessage());
                    return null;
                });

        return "Appointment with ID " + appointmentId + " has been completed.";
    }

    public List<AppointmentDotForAdminDashboard> getAllAppointments() {
        List<AppointmentModel> appointmentModels = appointmentRepository.findAll();
        return appointmentModels.stream().map(appointment -> new AppointmentDotForAdminDashboard(
                appointment.getAppointmentId(),
                appointment.getAppointmentTime(),
                appointment.getStatus().name(),
                appointment.getUserId(),
                appointment.getUsersFullName(),
                appointment.getUsersEmail(),
                appointment.getDoctorId(),
                appointment.getDoctorFullName(),
                appointment.getReason(),
                appointment.getCreatedAt()
        )).toList();
    }

    public List<UsersAppointmentsDto> getUserAppointments(String userId) {
        List<AppointmentModel> appointmentModels = appointmentRepository.findByUserId(userId);
        return appointmentModels.stream().map(appointment -> new UsersAppointmentsDto(
                appointment.getAppointmentId(),
                appointment.getStatus().name(),
                appointment.getDoctorId(),
                appointment.getDoctorFullName(),
                appointment.getDoctorSpecialization(),
                appointment.getAppointmentTime(),
                appointment.getReason()
        )).toList();
    }

    public List<DoctorAppointmentDto> getDoctorAppointments(String doctorId) {
        List<AppointmentModel> appointmentModels = appointmentRepository.findByDoctorId(doctorId);
        return appointmentModels.stream().map(appointment -> new DoctorAppointmentDto(
                appointment.getAppointmentId(),
                appointment.getStatus().name(),
                appointment.getUserId(),
                appointment.getUsersFullName(),
                appointment.getUsersEmail(),
                appointment.getReason(),
                appointment.getAppointmentTime()
        )).toList();
    }
}
