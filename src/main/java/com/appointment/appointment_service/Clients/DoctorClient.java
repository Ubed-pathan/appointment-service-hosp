package com.appointment.appointment_service.Clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service")
public interface DoctorClient {
    @GetMapping("/doctor/exists/{doctorId}")
    String isDocterExists(@PathVariable String doctorId);
}