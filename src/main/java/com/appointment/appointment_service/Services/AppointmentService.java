package com.appointment.appointment_service.Services;

import com.appointment.appointment_service.Dtos.AppointmentDto;
import com.appointment.appointment_service.Models.AppointmentModel;
import com.appointment.appointment_service.Repositories.AppointmentRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentService {

    private AppointmentRepository appointmentRepository;

    public String bookAppointment(AppointmentDto dto) {
        LocalDate appointmentDate = dto.appointmentTime().toLocalDate();
        LocalDateTime startOfDay = appointmentDate.atStartOfDay();
        LocalDateTime endOfDay = appointmentDate.atTime(23, 59, 59);

        // Check: has the user already booked with this doctor on the same day?
        boolean alreadyBooked = appointmentRepository.existsByUserIdAndDoctorIdAndAppointmentTimeBetween(
                dto.userId(),
                dto.doctorId(),
                startOfDay,
                endOfDay
        );

        if (alreadyBooked) {
            throw new RuntimeException("User already has an appointment with this doctor on the same day.");
        }

        // Check: is the time slot already taken by another user?
        boolean slotTaken = appointmentRepository.existsByDoctorIdAndAppointmentTime(
                dto.doctorId(),
                dto.appointmentTime()
        );

        if (slotTaken) {
            throw new RuntimeException("Appointment slot is already booked.");
        }
        AppointmentModel appointmentModel = new AppointmentModel();
        appointmentModel.setAppointmentTime(dto.appointmentTime());
        appointmentModel.setUserId(dto.userId());
        appointmentModel.setDoctorId(dto.doctorId());
        appointmentModel.setReason(dto.reason());
        appointmentRepository.save(appointmentModel);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a"); // 12-hour format with AM/PM
        String formattedTime = appointmentModel.getAppointmentTime().format(formatter);
        return "Appointment booked at " + formattedTime + ". Your appointment ID is " + appointmentModel.getAppointmentId();

    }
}
