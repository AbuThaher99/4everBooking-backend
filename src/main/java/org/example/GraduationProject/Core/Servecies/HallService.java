package org.example.GraduationProject.Core.Servecies;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.example.GraduationProject.Common.DTOs.GetReservationDTO;
import org.example.GraduationProject.Common.DTOs.HallDTO;
import org.example.GraduationProject.Common.DTOs.PaginationDTO;
import org.example.GraduationProject.Common.DTOs.ReservationDTO;
import org.example.GraduationProject.Common.Entities.*;
import org.example.GraduationProject.Common.Enums.HallCategory;
import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Common.Responses.HallResponse;
import org.example.GraduationProject.Core.Repsitories.*;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@AllArgsConstructor
public class HallService {
    private final HallRepository hallRepository;
    private final StorageService storageService;
    private final CustomerRepository customerRepository;
    private final HallOwnerRepository hallOwnerRepository;
    private final reservationsRepository reservationsRepository;
    private final EmailService emailService;

    @Transactional
    public HallResponse createHall(Hall hall ) throws  UserNotFoundException {
        if(hall.getImage() == null){
            throw new UserNotFoundException("Image is required");
        }
        if(hall.getProofFile() == null){
            throw new UserNotFoundException("Proof file is required");
        }
        hall.setImage(hall.getImage());
        hallRepository.save(hall);
        return new HallResponse("Hall created successfully");
    }

    @Transactional
    public String uploadImage(MultipartFile[] file) throws IOException, UserNotFoundException {
        return storageService.uploadMultiImageToFileSystem(file);
    }

    public HallResponse updateHall(HallDTO hall , Long OwnerId ) throws UserNotFoundException {
        Hall existingHall = hallRepository.findById(hall.getId()).orElseThrow(() ->
                new UserNotFoundException("Hall not found with id: " + hall.getId())
        );
        HallOwner hallOwner = hallOwnerRepository.findById(OwnerId).orElseThrow(() ->
                new UserNotFoundException("Hall Owner not found with id: " + OwnerId)
        );

        if(existingHall.getHallOwner().getId() != hallOwner.getId()){
            return new HallResponse("You are not allowed to update this hall");
        }

        existingHall.setName(hall.getName());
        existingHall.setLocation(hall.getLocation());
        existingHall.setCapacity(hall.getCapacity());
        existingHall.setDescription(hall.getDescription());
        existingHall.setPhone(hall.getPhone());
        existingHall.setServices(hall.getServices());
        existingHall.setLongitude(hall.getLongitude());
        existingHall.setLatitude(hall.getLatitude());
        hallRepository.save(existingHall);
        return  new HallResponse("Hall updated successfully");

    }

    public HallResponse deleteHall(Long id , Long OwnerId) throws UserNotFoundException {
        Hall hall = hallRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("Hall not found with id: " + id)
        );
        HallOwner hallOwner = hallOwnerRepository.findById(OwnerId).orElseThrow(() ->
                new UserNotFoundException("Hall Owner not found with id: " + OwnerId)
        );
        if(hall.getHallOwner().getId() != hallOwner.getId()){
            return new HallResponse("You are not allowed to delete this hall");
        }
        hall.setDeleted(true);
        hallRepository.save(hall);
        return new HallResponse("Hall deleted successfully");
    }

    @Transactional
    public PaginationDTO<Hall> getAll(int page, int size, String search, String location, Double minPrice, Double maxPrice, Integer minCapacity, Integer maxCapacity, String category) {
        if (page < 1) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size);

        if (search != null && search.isEmpty()) {
            search = null;
        }
        if (location != null && location.isEmpty()) {
            location = null;
        }
        if (minPrice == null) {
            minPrice = 0.0;
        }
        if (maxPrice == null) {
            maxPrice = Double.MAX_VALUE;
        }
        if (minCapacity == null) {
            minCapacity = 0; // Set to zero or an appropriate default value
        }
        if (maxCapacity == null) {
            maxCapacity = Integer.MAX_VALUE; // Set to maximum possible value
        }

        Page<Hall> hallPage = hallRepository.findAll(pageable, search, location, minPrice, maxPrice, minCapacity, maxCapacity, category);

        PaginationDTO<Hall> paginationDTO = new PaginationDTO<>();
        paginationDTO.setTotalElements(hallPage.getTotalElements());
        paginationDTO.setTotalPages(hallPage.getTotalPages());
        paginationDTO.setSize(hallPage.getSize());
        paginationDTO.setNumber(hallPage.getNumber() + 1);
        paginationDTO.setNumberOfElements(hallPage.getNumberOfElements());
        paginationDTO.setContent(hallPage.getContent());

        return paginationDTO;
    }



    @Transactional
    public Hall getHall(Long id) throws UserNotFoundException {
        return hallRepository.findById(id).orElseThrow(() ->
                new UserNotFoundException("Hall not found with id: " + id)
        );
    }

    @Transactional
    public PaginationDTO<Hall> getHallsByOwner(int page, int size ,Long ownerId) throws UserNotFoundException {

        hallOwnerRepository.findById(ownerId).orElseThrow(() ->
                new UserNotFoundException("Hall Owner not found with id: " + ownerId) );

        if (page < 1) {
            page = 1;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Hall> halls = hallRepository.findByOwnerId(pageable,ownerId);
        PaginationDTO<Hall> paginationDTO = new PaginationDTO<>();
        paginationDTO.setTotalElements(halls.getTotalElements());
        paginationDTO.setTotalPages(halls.getTotalPages());
        paginationDTO.setSize(halls.getSize());
        paginationDTO.setNumber(halls.getNumber() + 1);
        paginationDTO.setNumberOfElements(halls.getNumberOfElements());
        paginationDTO.setContent(halls.getContent());
        return paginationDTO;
    }

   public PaginationDTO<GetReservationDTO> findReservedHallByCustomer(int page, int size , Long CustomerId) throws UserNotFoundException {
         customerRepository.findById(CustomerId).orElseThrow(() ->
               new UserNotFoundException("Customer not found with id: " + CustomerId) );
       if (page < 1) {
           page = 1;
       }
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<GetReservationDTO> halls = reservationsRepository.findReservedCustomer(pageable,CustomerId);
            PaginationDTO<GetReservationDTO> paginationDTO = new PaginationDTO<>();
            paginationDTO.setTotalElements(halls.getTotalElements());
            paginationDTO.setTotalPages(halls.getTotalPages());
            paginationDTO.setSize(halls.getSize());
            paginationDTO.setNumber(halls.getNumber() + 1);
            paginationDTO.setNumberOfElements(halls.getNumberOfElements());
            paginationDTO.setContent(halls.getContent());
            return paginationDTO;

    }
    public PaginationDTO<GetReservationDTO> findReservedHallByOwner(int page , int size,Long ownerId) throws UserNotFoundException {
        hallOwnerRepository.findById(ownerId).orElseThrow(() ->
                new UserNotFoundException("Hall Owner not found with id: " + ownerId) );
        if (page < 1) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<GetReservationDTO> halls = reservationsRepository.findReservedHallOwner(pageable,ownerId);
        PaginationDTO<GetReservationDTO> paginationDTO = new PaginationDTO<>();
        paginationDTO.setTotalElements(halls.getTotalElements());
        paginationDTO.setTotalPages(halls.getTotalPages());
        paginationDTO.setSize(halls.getSize());
        paginationDTO.setNumber(halls.getNumber() + 1);
        paginationDTO.setNumberOfElements(halls.getNumberOfElements());
        paginationDTO.setContent(halls.getContent());

        return paginationDTO;
    }
    public GeneralResponse reserveHall(ReservationDTO reservationDTO) throws UserNotFoundException, MessagingException {
        Hall hall = hallRepository.findById(reservationDTO.getHallId()).orElseThrow(() ->
                new UserNotFoundException("Hall not found with id: " + reservationDTO.getHallId())
        );
        Customer customer = customerRepository.findById(reservationDTO.getCustomerId()).orElseThrow(() ->
                new UserNotFoundException("Customer not found with id: " + reservationDTO.getCustomerId())
        );

        HallCategory selectedCategory = reservationDTO.getSelectedCategory();
        Map<HallCategory, Double> categoriesMap = hall.getCategories();

        Double categoryPrice = categoriesMap.get(selectedCategory);
        if (reservationDTO.getTime().isBefore(LocalDateTime.now())) {
            throw new UserNotFoundException("Reservation time cannot be in the past");
        }
        if (categoryPrice == null) {
            throw new UserNotFoundException("Category not found or not available: " + selectedCategory);
        }

        // Handle null endTime: Set as null if not provided
        LocalDateTime endTime = reservationDTO.getEndTime();
        if (endTime != null && endTime.isBefore(reservationDTO.getTime())) {
            throw new UserNotFoundException("End time cannot be before start time");
        }

        double totalPrice = categoryPrice;

        long daysBetween = endTime != null ? ChronoUnit.DAYS.between(reservationDTO.getTime(), endTime) : 0;
        if (daysBetween > 0) {
            totalPrice = categoryPrice * daysBetween;
        }

        for (Map.Entry<String, Object> entry : reservationDTO.getServices().entrySet()) {
            if (entry.getValue() instanceof Integer) {
                totalPrice += (double) ((Integer) entry.getValue());
            } else {
                totalPrice += (double) entry.getValue();
            }
        }

        Reservations reservations = Reservations.builder()
                .date(reservationDTO.getTime())
                .endDate(endTime) // Save null if endTime is not provided
                .totalPrice(totalPrice)
                .chosenServices(reservationDTO.getServices())
                .customer(customer)
                .hall(hall)
                .category(selectedCategory.toString())
                .build();

        // Generate a random bill number in the format 'char-tenNumber'
        char randomChar = (char) ('A' + new Random().nextInt(26));
        long tenDigitNumber = Math.abs(new Random().nextLong()) % 10000000000L;
        String billNumber = randomChar + "-" + String.format("%010d", tenDigitNumber);

        String invoice = storageService.uploadInvoiceForReservation(reservations, billNumber);
        reservations.setInvoice(invoice);
        reservationsRepository.save(reservations);

        // Prepare email body
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; color: #333; }")
                .append("h2 { color: #0056b3; }")
                .append(".details-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }")
                .append(".details-table th, .details-table td { padding: 10px; border-bottom: 1px solid #ddd; }")
                .append(".details-table th { background-color: #f2f2f2; text-align: left; }")
                .append(".footer { margin-top: 20px; font-size: 0.9em; color: #777; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<h2>Reservation Confirmation</h2>")
                .append("<p>Dear ").append(customer.getUser().getFirstName()).append(",</p>")
                .append("<p>Thank you for your reservation. Below are the details of your booking:</p>")

                // Start the table
                .append("<table class='details-table'>")
                .append("<tr><th>Bill Number:</th><td>").append(billNumber).append("</td></tr>")
                .append("<tr><th>Hall Name:</th><td>").append(hall.getName()).append("</td></tr>")
                .append("<tr><th>Category:</th><td>").append(selectedCategory).append("</td></tr>")
                .append("<tr><th>Hall Location:</th><td>").append(hall.getLocation()).append("</td></tr>")
                .append("<tr><th>Reservation Date:</th><td>").append(reservationDTO.getTime()).append("</td></tr>");

        // Handle null end time in the email (display "N/A" if null)
        if (endTime != null) {
            emailBody.append("<tr><th>End Date:</th><td>").append(endTime).append("</td></tr>");
        } else {
            emailBody.append("<tr><th>End Date:</th><td>N/A</td></tr>");
        }

        emailBody.append("<tr><th>Total Price:</th><td>").append(totalPrice).append(" NIS</td></tr>")
                .append("</table>") // End the table

                .append("<p><strong>Chosen Services:</strong></p>")
                .append("<ul>");

        // Loop through services and append them to the email body
        for (Map.Entry<String, Object> service : reservationDTO.getServices().entrySet()) {
            emailBody.append("<li>").append(service.getKey()).append(": ").append(service.getValue()).append("</li>");
        }

        emailBody.append("</ul>")
                .append("<p>We look forward to seeing you!</p>")
                .append("<p>Best regards,<br/>The 4everBooking Team</p>")
                .append("<div class='footer'>This is an automated email, please do not reply.</div>")
                .append("</body>")
                .append("</html>");

        emailService.sentNotificationEmail(customer.getUser().getEmail(),
                "Reservation Confirmation", emailBody.toString());

        hall.setReserved(true);
        hallRepository.save(hall);

        return GeneralResponse.builder().message("Hall reserved successfully").build();
    }


    public List<Integer> getReservedDays(Long hallId, int year, int month) {
        List<Reservations> reservations = hallRepository.findReservationsInMonth(hallId, year, month);

        Set<Integer> reservedDays = new HashSet<>();

        for (Reservations reservation : reservations) {
            LocalDateTime start = reservation.getDate();
            LocalDateTime end = reservation.getEndDate() != null ? reservation.getEndDate() : start;

            while (!start.isAfter(end)) {
                if (start.getYear() == year && start.getMonthValue() == month) {
                    reservedDays.add(start.getDayOfMonth());
                }
                start = start.plusDays(1);
            }
        }

        return new ArrayList<>(reservedDays);
    }

    public PaginationDTO<Hall> getDeletedHallsByHallOwner(int page, int size, Long hallOwnerId) throws UserNotFoundException {
        hallOwnerRepository.findById(hallOwnerId).orElseThrow(() ->
                new UserNotFoundException("Hall Owner not found with id: " + hallOwnerId) );
        if (page < 1) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Hall> halls = hallOwnerRepository.getDeletedHallsByHallOwner(pageable, hallOwnerId);
        PaginationDTO<Hall> paginationDTO = new PaginationDTO<>();
        paginationDTO.setTotalElements(halls.getTotalElements());
        paginationDTO.setTotalPages(halls.getTotalPages());
        paginationDTO.setSize(halls.getSize());
        paginationDTO.setNumber(halls.getNumber() + 1);
        paginationDTO.setNumberOfElements(halls.getNumberOfElements());
        paginationDTO.setContent(halls.getContent());
        return paginationDTO;

    }

    public GeneralResponse restoreHall(Long hallId, Long hallOwnerId) throws UserNotFoundException {
        Hall hall = hallRepository.findById(hallId).orElseThrow(() ->
                new UserNotFoundException("Hall not found with id: " + hallId)
        );
        HallOwner hallOwner = hallOwnerRepository.findById(hallOwnerId).orElseThrow(() ->
                new UserNotFoundException("Hall Owner not found with id: " + hallOwnerId)
        );
        if (hall.getHallOwner().getId() != hallOwner.getId()) {
            return GeneralResponse.builder().message("You are not allowed to restore this hall").build();
        }
        if(hall.isDeleted() == false){
            return GeneralResponse.builder().message("Hall is not deleted").build();
        }
        hall.setDeleted(false);
        hallRepository.save(hall);
        return GeneralResponse.builder().message("Hall restored successfully").build();
    }
    @Transactional
    public String uploadHallProve( MultipartFile file) throws IOException {
       return storageService.uploadProveHall(file);

    }

    public PaginationDTO<Hall> getHallsIsNotProcessed(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Hall> halls = hallRepository.findAllHallsIsProcessed(pageable);
        PaginationDTO<Hall> paginationDTO = new PaginationDTO<>();
        paginationDTO.setTotalElements(halls.getTotalElements());
        paginationDTO.setTotalPages(halls.getTotalPages());
        paginationDTO.setSize(halls.getSize());
        paginationDTO.setNumber(halls.getNumber() + 1);
        paginationDTO.setNumberOfElements(halls.getNumberOfElements());
        paginationDTO.setContent(halls.getContent());
        return paginationDTO;
    }

    public GeneralResponse processHall(Long hallId) throws UserNotFoundException {
        Hall hall = hallRepository.findById(hallId).orElseThrow(() ->
                new UserNotFoundException("Hall not found with id: " + hallId)
        );
        hall.setProcessed(true);
        hallRepository.save(hall);
        return GeneralResponse.builder().message("Hall processed successfully").build();
    }
}
