package com.appointment.appointment_service.Clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service")
public interface DoctorServiceClient {
    @GetMapping("/doctors/{doctorId}")
    boolean isDocterExists(@PathVariable String doctorId);
}