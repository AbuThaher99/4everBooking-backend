package org.example.GraduationProject.Core.Servecies;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.GraduationProject.Common.DTOs.*;
import org.example.GraduationProject.Common.Entities.*;
import org.example.GraduationProject.Common.Enums.Role;
import org.example.GraduationProject.Common.Enums.TokenType;
import org.example.GraduationProject.Common.Responses.AuthenticationResponse;
import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Core.Repsitories.*;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.example.GraduationProject.WebApi.config.JwtService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService  {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final CustomerRepository  customerRepository;
    private final HallOwnerRepository hallOwnerRepository;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final EmailRepository emailRepository;
    private final StorageService storageService;

    @Transactional
    public AuthenticationResponse authenticate(LoginDTO request) throws UserNotFoundException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(
                        () -> new UserNotFoundException("User not found")
                );
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
    public AuthenticationResponse addAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ADMIN);
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .message("Admin created successfully")
                .build();
    }

    @Transactional
    public GeneralResponse UpdateUser(Long id, UserDTO user) throws UserNotFoundException {
        User oldUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        oldUser.setPhone(user.getPhone());
        oldUser.setFirstName(user.getFirstName());
        oldUser.setLastName(user.getLastName());
        oldUser.setAddress(user.getAddress());
        oldUser.setDateOfBirth(user.getDateOfBirth());
        userRepository.save(oldUser);
        return GeneralResponse.builder()
                .message("User updated successfully")
                .build();
    }

    @Transactional
    public GeneralResponse UpdateHallOwner(Long id, HallOwnerDTO user) throws UserNotFoundException {


        User oldUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        oldUser.setPhone(user.getPhone());
        oldUser.setFirstName(user.getFirstName());
        oldUser.setLastName(user.getLastName());
        oldUser.setAddress(user.getAddress());
        oldUser.setDateOfBirth(user.getDateOfBirth());
        userRepository.save(oldUser);

        HallOwner hallOwner = hallOwnerRepository.findById(oldUser.getHallOwner().getId()).orElseThrow(
                () -> new UserNotFoundException("Hall Owner not found")
        );
        hallOwner.setCompanyName(user.getCompanyName());
        hallOwnerRepository.save(hallOwner);
        return GeneralResponse.builder()
                .message("Hall Owner updated successfully")
                .build();
    }


    @Transactional
    public GeneralResponse UpdateCustomer(Long id, CustomerDTO user) throws UserNotFoundException {

        User oldUser = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        oldUser.setPhone(user.getPhone());
        oldUser.setFirstName(user.getFirstName());
        oldUser.setLastName(user.getLastName());
        oldUser.setAddress(user.getAddress());
        oldUser.setDateOfBirth(user.getDateOfBirth());
        userRepository.save(oldUser);
        return GeneralResponse.builder()
                .message("Customer updated successfully")
                .build();
    }


    @Transactional
    public GeneralResponse DeleteUser(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        user.setDeleted(true);
        userRepository.save(user);
        return GeneralResponse.builder()
                .message("User deleted successfully")
                .build();
    }

    @Transactional
    public PaginationDTO<User> GetAllUsers(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> users = userRepository.findAll(pageable);
        PaginationDTO<User> paginationDTO = new PaginationDTO<>();
        paginationDTO.setTotalElements(users.getTotalElements());
        paginationDTO.setTotalPages(users.getTotalPages());
        paginationDTO.setSize(users.getSize());
        paginationDTO.setNumber(users.getNumber() + 1);
        paginationDTO.setNumberOfElements(users.getNumberOfElements());
        paginationDTO.setContent(users.getContent());
        return paginationDTO;
    }

    @Transactional
    public User GetUser(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
    }

    public AuthenticationResponse CustomerRegister(Customer customer) {
        User  user = customer.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.CUSTOMER);
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        Customer savedCustomer = Customer.builder()
                .user(savedUser)
                .build();
        customerRepository.save(savedCustomer);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .message("Customer created successfully")
                .build();
    }

    public AuthenticationResponse HallOwnerRegister(HallOwner  hallOwner) {
        User user = hallOwner.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.HALL_OWNER);
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        hallOwnerRepository.save(HallOwner.builder()
                .user(savedUser)
                .companyName(hallOwner.getCompanyName())
                .build());

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .message("Hall Owner created successfully")
                .build();
    }
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    public User extractUserFromToken(String token) {
        String username = jwtService.extractUsername(token);
        return userRepository.findByEmail(username).orElse(null);
    }


    @Transactional
    public GeneralResponse resetPassword(String email, String password) throws UserNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return GeneralResponse.builder()
                .message("Password reset successfully")
                .build();
    }

    @Transactional
    public void sendVerificationCode(String email) throws UserNotFoundException, MessagingException {
        var userEmail = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String verificationCode = UUID.randomUUID().toString();
        Email emailEntity = Email.builder()
                .email(email)
                .verificationCode(verificationCode)
                .verified(false)
                .build();
        emailRepository.save(emailEntity);
        String verificationUrl = "https://website-4everbooking-backend99-461f6fb275e9.herokuapp.com/resetPasswordPage?verificationCode=" + verificationCode + "&email=" + email;
        emailService.sendVerificationEmail(email, "Email Verification", verificationUrl);
    }

    @Transactional
    public GeneralResponse verifyCodeAndResetPassword(String email, String verificationCode, String newPassword) throws UserNotFoundException {
        Email emailEntity = emailRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email not found"));
        if (emailEntity.getVerificationCode().equals(verificationCode)) {
            emailEntity.setVerified(true);
            emailRepository.save(emailEntity);
        } else {
            throw new UserNotFoundException("Invalid verification code ");
        }
        return resetPassword(email, newPassword);
    }


    @Transactional
    public AuthenticationResponse ChangePassword(String email, String oldPassword, String newPassword) throws UserNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            var savedUser = userRepository.save(user);
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            saveUserToken(savedUser, jwtToken);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .message("Password changed successfully")
                    .build();
        } else {
            throw new UserNotFoundException("Invalid old password");
        }
    }

    @Transactional
    public boolean expiredToken(Long id, String token)  {
        boolean userToken = tokenRepository.findValidTokenByUserAndToken(id, token).isPresent();

            if(userToken){
                return false;
            }
        return true;
    }

    @Transactional
    public GeneralResponse uploadImageToProfile(Long id , MultipartFile image) throws UserNotFoundException, IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String im = storageService.uploadImageToFileProfile(image);
        user.setImage(im);
        userRepository.save(user);
        return GeneralResponse.builder()
                .message("Image uploaded successfully")
                .build();
    }
}
