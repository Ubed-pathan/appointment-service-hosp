package com.appointment.appointment_service.Repositories;

import com.appointment.appointment_service.Models.AppointmentModel;
import com.appointment.appointment_service.Models.FeedbackModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentModel, String> {
    // Find all appointments for a specific doctor
    List<AppointmentModel> findByDoctorId(String doctorId);


    // Find appointments based on date/time string if you format it properly
    List<AppointmentModel> findByAppointmentStartTime(LocalDateTime appointmentStartTime);

    boolean existsByDoctorIdAndAppointmentStartTime(String doctorId, LocalDateTime appointmentStartTime);

    boolean existsByUserIdAndAppointmentStartTime(String userId, LocalDateTime appointmentStartTime);

    boolean existsByUserIdAndDoctorIdAndAppointmentStartTimeBetween(
            String userId,
            String doctorId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );


    List<AppointmentModel> findByUserId(String userId);

    List<AppointmentModel> findByDoctorUsername(String doctorUsername);

    // Check if any existing appointment for the doctor overlaps the proposed range
    boolean existsByDoctorIdAndAppointmentEndTimeGreaterThanAndAppointmentStartTimeLessThan(
            String doctorId,
            LocalDateTime proposedStart,
            LocalDateTime proposedEnd
    );
}
