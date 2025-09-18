package com.appointment.appointment_service.Repositories;

import com.appointment.appointment_service.Models.FeedbackModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackModel, String> {
    List<FeedbackModel> findByAppointment_AppointmentId(String appointmentId);

    List<FeedbackModel> findByDoctorId(String doctorId);
}
