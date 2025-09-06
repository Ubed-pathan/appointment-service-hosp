package com.appointment.appointment_service.Clients;


import com.appointment.appointment_service.Dtos.DoctorDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service")
public interface DoctorClient {
    @GetMapping("/doctor/exists/{doctorId}")
    DoctorDto isDocterExists(@PathVariable String doctorId);
}