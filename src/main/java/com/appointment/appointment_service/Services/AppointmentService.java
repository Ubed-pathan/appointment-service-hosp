package com.appointment.appointment_service.Services;

import com.appointment.appointment_service.Clients.DoctorClient;
import com.appointment.appointment_service.Clients.UserClient;
import com.appointment.appointment_service.Dtos.*;
import com.appointment.appointment_service.Models.AppointmentModel;
import com.appointment.appointment_service.Models.FeedbackModel;
import com.appointment.appointment_service.Repositories.AppointmentRepository;
import com.appointment.appointment_service.Repositories.FeedbackRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final FeedbackRepository feedbackRepository;
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
        // Persist email/fullName from request (fallback to validation response if request blank)
        String safeFullName = (dto.usersFullName() != null && !dto.usersFullName().isBlank())
                ? dto.usersFullName() : isUserValid.usersFullName();
        String safeEmail = (dto.usersEmail() != null && !dto.usersEmail().isBlank())
                ? dto.usersEmail() : isUserValid.usersEmail();
        appointmentModel.setUsersFullName(safeFullName);
        appointmentModel.setUsersEmail(safeEmail);
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
                safeFullName,
                safeEmail,
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
        return appointmentModels.stream().map(appointment -> {
            List<FeedbackDto> feedbacks = feedbackRepository.findByAppointment_AppointmentId(appointment.getAppointmentId())
                    .stream()
                    .map(f -> new FeedbackDto(
                            f.getAppointment().getAppointmentId(),
                            f.getDoctorId(),
                            f.getReview(),
                            f.getRating()
                    )).toList();
            return new UsersAppointmentsDto(
                    appointment.getAppointmentId(),
                    appointment.getStatus().name(),
                    appointment.getDoctorId(),
                    appointment.getDoctorFullName(),
                    appointment.getDoctorSpecialization(),
                    appointment.getAppointmentTime(),
                    appointment.getReason(),
                    appointment.isDidUserGiveFeedback(),
                    feedbacks
            );
        }).toList();
    }

    public List<DoctorAppointmentDto> getDoctorAppointments(String doctorUsername) {
        List<AppointmentModel> appointmentModels = appointmentRepository.findByDoctorUsername(doctorUsername);
        return appointmentModels.stream().map(appointment -> {
            List<FeedbackDto> feedbacks = feedbackRepository.findByAppointment_AppointmentId(appointment.getAppointmentId())
                    .stream()
                    .map(f -> new FeedbackDto(
                            f.getAppointment().getAppointmentId(),
                            f.getDoctorId(),
                            f.getReview(),
                            f.getRating()
                    )).toList();
            return new DoctorAppointmentDto(
                    appointment.getAppointmentId(),
                    appointment.getStatus().name(),
                    appointment.getUserId(),
                    appointment.getUsersFullName(),
                    appointment.getUsersEmail(),
                    appointment.getReason(),
                    appointment.getAppointmentTime(),
                    appointment.isDidUserGiveFeedback(),
                    feedbacks
            );
        }).toList();
    }

    public List<PatientsOfDoctorDto> getAllPatientsOfDoctor(String doctorUsername) {
        List<AppointmentModel> appointmentModels = appointmentRepository.findByDoctorUsername(doctorUsername);
        return appointmentModels.stream().map(appointment -> new PatientsOfDoctorDto(
                appointment.getAppointmentId(),
                appointment.getAppointmentTime(),
                appointment.getStatus().name(),
                // Using userId as userName placeholder since username is not stored on appointment
                appointment.getUserId(),
                appointment.getUsersFullName(),
                appointment.getUsersEmail(),
                appointment.getReason()
        )).toList();
    }

    public void userFeedback(@Valid FeedbackDto dto) {
        AppointmentModel appointment = appointmentRepository.findById(dto.appointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (!appointment.getDoctorId().equals(dto.doctorId())) {
            throw new RuntimeException("User is not authorized to provide feedback for this appointment.");
        }

        if (appointment.getStatus() != AppointmentModel.AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Feedback can only be provided for completed appointments.");
        }

        FeedbackModel feedback = new FeedbackModel();
        feedback.setDoctorId(dto.doctorId());
        feedback.setRating(dto.rating());
        feedback.setReview(dto.review());
        feedback.setAppointment(appointment); // appointmentModel must be fetched or created

        feedbackRepository.save(feedback);
        appointment.setDidUserGiveFeedback(true);
        appointmentRepository.save(appointment);
    }

    public List<AdminFeedbackDto> getFeedbacksForAdmin(String doctorId) {
        List<FeedbackModel> feedbacks = feedbackRepository.findByDoctorId(doctorId);
        return feedbacks.stream().map(fb -> {
            AppointmentModel appt = fb.getAppointment();
            return new AdminFeedbackDto(
                appt.getAppointmentId(),
                appt.getAppointmentTime(),
                appt.getUsersFullName(),
                appt.getUsersEmail(),
                appt.getDoctorFullName(),
                fb.getRating(),
                fb.getReview(),
                fb.getFeedbackId()
            );
        }).toList();
    }


    public void deleteFeedback(String feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new RuntimeException("Feedback not found.");
        }
        feedbackRepository.deleteById(feedbackId);
    }
}
