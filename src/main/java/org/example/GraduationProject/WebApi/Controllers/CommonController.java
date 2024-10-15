package org.example.GraduationProject.WebApi.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.GraduationProject.Common.Responses.AuthenticationResponse;
import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Core.Servecies.AuthenticationService;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final AuthenticationService authenticationService;
    @PostMapping("/changePassword")
    public ResponseEntity<AuthenticationResponse> changePassword(@RequestParam String email,
                                                                 @RequestParam String oldPassword,
                                                                 @RequestParam String newPassword) throws UserNotFoundException {
        AuthenticationResponse response = authenticationService.ChangePassword(email, oldPassword, newPassword);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/uploadImageToProfile")
    public ResponseEntity<GeneralResponse> uploadImageToProfile(@RequestParam Long id,
                                                                @RequestPart("image") MultipartFile image) throws UserNotFoundException, IOException {
        GeneralResponse response = authenticationService.uploadImageToProfile(id, image);
        return ResponseEntity.ok(response);
    }
}
