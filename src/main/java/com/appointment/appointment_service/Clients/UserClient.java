package com.appointment.appointment_service.Clients;

import com.appointment.appointment_service.Dtos.UserVerificationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/user/isValid/")
    UserVerificationDto isValidUser(UserVerificationDto Dto);
}
