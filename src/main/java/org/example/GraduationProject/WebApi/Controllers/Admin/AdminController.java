package org.example.GraduationProject.WebApi.Controllers.Admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.GraduationProject.Common.DTOs.PaginationDTO;
import org.example.GraduationProject.Common.DTOs.UserDTO;
import org.example.GraduationProject.Common.Entities.Customer;
import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.HallOwner;
import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Common.Responses.AuthenticationResponse;
import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Core.Servecies.AuthenticationService;
import org.example.GraduationProject.Core.Servecies.HallService;
import org.example.GraduationProject.SessionManagement;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController extends SessionManagement {
    private final AuthenticationService authenticationService;
    private final HallService hallService;

    @Operation(summary = "add new admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Admin added successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"user\":{\"password\":\"$2a$10$E6K7Z5M\",\"firstName\":\"mohammad\",\"lastName\":\"mohammad\",\"address\":\"mohammad\",\"phone\":\"0599782941\",\"dateOfBirth\":\"2002-07-22\",\"email\":\"m@gmail.com\"}}"))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\":400,\"message\":\"Validation error\",\"errors\":[\"Password cannot be blank\",\"Email cannot be blank\"]}"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\":404,\"message\":\"User not found\"}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/addAdmin")
    public AuthenticationResponse addAdmin(@RequestBody @Valid User admin, HttpServletRequest httpServletRequest) throws UserNotFoundException {
        String token = authenticationService.extractToken(httpServletRequest);
        User user = authenticationService.extractUserFromToken(token);
        validateLoggedInAdmin(user);
        return authenticationService.addAdmin(admin);
    }


    @PutMapping("/updateUser/{id}")
    public GeneralResponse updateUser(@PathVariable Long id, @RequestBody @Valid UserDTO user, HttpServletRequest httpServletRequest) throws UserNotFoundException {
        String token = authenticationService.extractToken(httpServletRequest);
        User loggedInUser = authenticationService.extractUserFromToken(token);
        validateLoggedInAdmin(loggedInUser);
        return authenticationService.UpdateUser(id,user);
    }

    @DeleteMapping("/deleteUser/{id}")
    public GeneralResponse deleteUser(@PathVariable Long id, HttpServletRequest httpServletRequest) throws UserNotFoundException {
        String token = authenticationService.extractToken(httpServletRequest);
        User loggedInUser = authenticationService.extractUserFromToken(token);
        validateLoggedInAdmin(loggedInUser);
        return authenticationService.DeleteUser(id);
    }


    @GetMapping("/users")
    public PaginationDTO<User> getAllUsers(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size, HttpServletRequest httpServletRequest) throws UserNotFoundException {
        String token = authenticationService.extractToken(httpServletRequest);
        User loggedInUser = authenticationService.extractUserFromToken(token);
        validateLoggedInAdmin(loggedInUser);
        return authenticationService.GetAllUsers(page,size);
    }

    @GetMapping("/getUser/{id}")
    public User getUser(@PathVariable Long id, HttpServletRequest httpServletRequest) throws UserNotFoundException {
        String token = authenticationService.extractToken(httpServletRequest);
        User loggedInUser = authenticationService.extractUserFromToken(token);
        validateLoggedInAdmin(loggedInUser);
        return authenticationService.GetUser(id);
    }

    @GetMapping("/getAllHallIsProcessed")
    public PaginationDTO<Hall> getAllHallIsProcessed(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     HttpServletRequest httpServletRequest) throws UserNotFoundException {
        String token = authenticationService.extractToken(httpServletRequest);
        User loggedInUser = authenticationService.extractUserFromToken(token);
        validateLoggedInAdmin(loggedInUser);
        return hallService.getHallsIsNotProcessed(page,size);
    }

    @PutMapping("/processHall/{id}")
    public GeneralResponse processHall(@PathVariable Long id,
                                       HttpServletRequest httpServletRequest) throws UserNotFoundException {
        String token = authenticationService.extractToken(httpServletRequest);
        User loggedInUser = authenticationService.extractUserFromToken(token);
        validateLoggedInAdmin(loggedInUser);
        return hallService.processHall(id);
    }

}
