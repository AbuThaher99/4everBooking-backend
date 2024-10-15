package org.example.GraduationProject.WebApi.Controllers.Customer;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.GraduationProject.Common.DTOs.*;
import org.example.GraduationProject.Common.Entities.*;
import org.example.GraduationProject.Common.Responses.AuthenticationResponse;
import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Core.Repsitories.CustomerRepository;
import org.example.GraduationProject.Core.Servecies.AuthenticationService;
import org.example.GraduationProject.Core.Servecies.CustomerService;
import org.example.GraduationProject.Core.Servecies.HallService;
import org.example.GraduationProject.Core.Servecies.RecommendationService;
import org.example.GraduationProject.SessionManagement;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController  extends SessionManagement {

    private final AuthenticationService authenticationService;
    private final HallService hallService;
    private final RecommendationService recommendationService;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    @GetMapping("/getAll")
    public PaginationDTO<GetReservationDTO> getAll(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "") Long customerId) throws UserNotFoundException {
        return hallService.findReservedHallByCustomer(page, size, customerId);
    }

    @PostMapping("/reserveHall")
    public GeneralResponse reserveHall( @RequestBody ReservationDTO reservationDTO) throws UserNotFoundException, MessagingException {
        return hallService.reserveHall(reservationDTO);
    }

    @PutMapping("updateProfile/{id}")
    public GeneralResponse updateProfile(@PathVariable Long id, @RequestBody @Valid CustomerDTO customerDTO) throws UserNotFoundException {
        return authenticationService.UpdateCustomer(id, customerDTO);
    }


    @GetMapping("/recommendhalls")
    public List<Hall> recommendHallsForUser(@AuthenticationPrincipal User user) {
        return recommendationService.recommendHalls(user);
    }
     @PutMapping("/rateHall")
    public GeneralResponse rateHall(@RequestBody RatingDTO ratingDTO) throws UserNotFoundException {
        return recommendationService.rateHall(ratingDTO);
    }

    @GetMapping("/getHallRatingbyUser")
    public ResponseEntity<UserHallRatings> getHallRatingByUser(@RequestParam Long userId, @RequestParam Long hallId) throws UserNotFoundException {
        return ResponseEntity.ok(recommendationService.getRating(userId, hallId));
    }
    @GetMapping("getCustomerByUserId/{userId}")
    public Long getCustomerByUserId(@PathVariable Long userId) {
        return customerRepository.getCustomerByUserId(userId);
    }

    @PostMapping("/{userId}/favorites")
    public ResponseEntity<String> addFavoriteHall(@PathVariable Long userId, @RequestBody Hall hall) throws UserNotFoundException {
        customerService.addFavoriteHall(userId, hall);
        return ResponseEntity.ok("Hall added to favorites");
    }

    @DeleteMapping("/{userId}/favorites")
    public ResponseEntity<String> removeFavoriteHall(@PathVariable Long userId, @RequestBody Hall hall) throws UserNotFoundException {
        customerService.removeFavoriteHall(userId, hall);
        return ResponseEntity.ok("Hall removed from favorites");
    }
    @GetMapping("/{userId}/favorites")
    public ResponseEntity<PaginationDTO<Hall>> getFavoriteHalls(@PathVariable Long userId,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        try {
            PaginationDTO<Hall> paginatedHalls = customerService.getFavoriteHalls(userId, page, size);
            return ResponseEntity.ok(paginatedHalls);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}