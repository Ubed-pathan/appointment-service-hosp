package com.appointment.appointment_service.Repositories;

import com.appointment.appointment_service.Models.AppointmentModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<AppointmentModel, String> {
    // Find all appointments for a specific doctor
    List<AppointmentModel> findByDoctorId(String doctorId);


    // Find appointments based on date/time string if you format it properly
    List<AppointmentModel> findByAppointmentTime(LocalDateTime appointmentTime);

    boolean existsByDoctorIdAndAppointmentTime(String doctorId, LocalDateTime appointmentTime);

    boolean existsByUserIdAndAppointmentTime(String userId, LocalDateTime appointmentTime);

    boolean existsByUserIdAndDoctorIdAndAppointmentTimeBetween(
            String userId,
            String doctorId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );


    List<AppointmentModel> findByUserId(String userId);

    List<AppointmentModel> findByDoctorUsername(String doctorUsername);
}
