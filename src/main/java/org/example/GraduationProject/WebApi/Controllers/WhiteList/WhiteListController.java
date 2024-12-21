package org.example.GraduationProject.WebApi.Controllers.WhiteList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.GraduationProject.Common.DTOs.PaginationDTO;
import org.example.GraduationProject.Common.Entities.Customer;
import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.HallOwner;
import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Common.Enums.HallCategory;
import org.example.GraduationProject.Common.Responses.AuthenticationResponse;
import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Core.Servecies.AuthenticationService;
import org.example.GraduationProject.Core.Servecies.HallService;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/whitelist")
@RequiredArgsConstructor
public class WhiteListController {
    private final AuthenticationService authenticationService;
    private final HallService hallService;
    @Operation(summary = "Register a new customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer registered successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"user\":{\"password\":\"$2a$10$E6K7Z5M\",\"firstName\":\"mohammad\",\"lastName\":\"mohammad\",\"address\":\"mohammad\",\"phone\":\"0599782941\",\"dateOfBirth\":\"2002-07-22\",\"email\":\"m@gmail.com\"}}"))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Validation error\",\"errors\":[\"Password cannot be blank\",\"Email cannot be blank\"]}"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\":404,\"message\":\"User not found\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/CustomerRegister")
    public AuthenticationResponse CustomerRegister(@RequestBody @Valid Customer customer) throws UserNotFoundException {
        return authenticationService.CustomerRegister(customer);
    }

    @Operation(summary = "Register a new hall owner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "hall owner registered successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"companyName\":\"AbuThaher\",\"user\":{\"password\":\"$2a$10$E6K7Z5M\",\"firstName\":\"mohammad\",\"lastName\":\"mohammad\",\"address\":\"mohammad\",\"phone\":\"0599782941\",\"dateOfBirth\":\"2002-07-22\",\"email\":\"m@gmail.com\"}}"))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Validation error\",\"errors\":[\"Password cannot be blank\",\"Email cannot be blank\"]}"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\":404,\"message\":\"User not found\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/RegisterHallOwner")
    public AuthenticationResponse HallOwnerRegister(@RequestBody @Valid HallOwner hallOwner) throws UserNotFoundException {
        return authenticationService.HallOwnerRegister(hallOwner);
    }

    @GetMapping("/getAll")
    public PaginationDTO<Hall> getAll(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "", required = false) String search,
                                      @RequestParam(defaultValue = "", required = false) String location,
                                      @RequestParam(defaultValue = "0") Double minPrice,
                                      @RequestParam(defaultValue = "10000000") Double maxPrice,
                                      @RequestParam(defaultValue = "0") Integer minCapacity,
                                      @RequestParam(defaultValue = "2147483647") Integer maxCapacity,
                                      @RequestParam(required = false) HallCategory category,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                      @RequestParam(defaultValue = "false") boolean sortByRecommendation,
                                      @RequestParam(required = false) Long userId,
                                      @RequestParam(defaultValue = "false") Boolean filterByProximity,
                                      @RequestParam(required = false) Double latitude,
                                      @RequestParam(required = false) Double longitude,
                                      @RequestParam(defaultValue = "15") double radius,
                                      @RequestParam(defaultValue = "false") Boolean sortByPrice )  {

        // Convert the category to a string if it's provided
        String categoryStr = (category != null) ? category.name() : null;

        // Call the service method with the added date range parameters
        return hallService.getAll(page, size, search, location, minPrice, maxPrice, minCapacity, maxCapacity, categoryStr, startDate, endDate, sortByRecommendation, userId, filterByProximity, latitude, longitude, radius,sortByPrice);
    }







    @GetMapping("/{id}")
    public Hall getHall(@PathVariable Long id) throws UserNotFoundException {
        return hallService.getHall(id);
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestParam String email) throws UserNotFoundException, MessagingException, MessagingException {
        authenticationService.sendVerificationCode(email);
        return ResponseEntity.ok("Verification code sent to email");
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<GeneralResponse> verifyCodeAndResetPassword(@RequestParam String email,
                                                                      @RequestParam String verificationCode,
                                                                      @RequestBody String newPassword
    ) throws UserNotFoundException {
        GeneralResponse response = authenticationService.verifyCodeAndResetPassword(
                email, verificationCode, newPassword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUser")
    public User getUser(@RequestHeader("Authorization") String request) {
        String token = request.replace("Bearer ", "");
        return authenticationService.extractUserFromToken(token);
    }

    @GetMapping("/{hallId}/reserved-days")
    public ResponseEntity<List<Integer>> getReservedDays(@PathVariable Long hallId,
                                                         @RequestParam int year,
                                                         @RequestParam int month) {
        List<Integer> reservedDays = hallService.getReservedDays(hallId, year, month);
        return ResponseEntity.ok(reservedDays);
    }

}
