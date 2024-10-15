package org.example.GraduationProject.WebApi.Controllers;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.GraduationProject.Common.DTOs.LoginDTO;
import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Common.Responses.AuthenticationResponse;
import org.example.GraduationProject.Core.Servecies.AuthenticationService;
import org.example.GraduationProject.Core.Servecies.LogoutService;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.example.GraduationProject.WebApi.config.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;
    private final LogoutService logoutService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginDTO request) throws UserNotFoundException {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        logoutService.logout(request, response, authentication);
        return ResponseEntity.status(HttpStatus.OK).body("Logged out successfully");
    }


}
