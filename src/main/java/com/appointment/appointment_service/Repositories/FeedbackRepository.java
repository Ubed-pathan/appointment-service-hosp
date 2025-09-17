package com.appointment.appointment_service.Repositories;

import com.appointment.appointment_service.Dtos.FeedbackDto;
import com.appointment.appointment_service.Models.FeedbackModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<FeedbackModel, String> {
}
