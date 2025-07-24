package com.appointment.appointment_service.Repositories;

import com.appointment.appointment_service.Models.AppointmentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<AppointmentModel, String> {
}
