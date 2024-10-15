package org.example.GraduationProject.WebApi.Controllers.HallOwner;

import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.GraduationProject.Common.DTOs.GetReservationDTO;
import org.example.GraduationProject.Common.DTOs.HallDTO;
import org.example.GraduationProject.Common.DTOs.HallOwnerDTO;
import org.example.GraduationProject.Common.DTOs.PaginationDTO;
import org.example.GraduationProject.Common.Entities.FileData;
import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.HallOwner;
import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Common.Responses.AuthenticationResponse;
import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Common.Responses.HallResponse;
import org.example.GraduationProject.Core.Repsitories.HallOwnerRepository;
import org.example.GraduationProject.Core.Servecies.AuthenticationService;
import org.example.GraduationProject.Core.Servecies.HallService;
import org.example.GraduationProject.Core.Servecies.StorageService;
import org.example.GraduationProject.SessionManagement;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/hallOwner")
@RequiredArgsConstructor
public class HallOwnerController extends SessionManagement {

    private final HallService hallService;
    private final AuthenticationService authenticationService;
    private final HallOwnerRepository hallOwnerRepository;
    private final StorageService storageService;

    @Operation(summary = "Create a new hall")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Hall created successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"message\":\"Hall created successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\n" +
                                            "  \"capacity\": 250,\n" +
                                            "  \"description\": \"nice venue\",\n" +
                                            "  \"hallOwner\": {\n" +
                                            "    \"id\": 1\n" +
                                            "  },\n" +
                                            "  \"location\": \"Ramallah\",\n" +
                                            "  \"name\": \"Mohammad\",\n" +
                                            "  \"phone\": \"0569482508\",\n" +
                                            "  \"price\": 6000,\n" +
                                            "  \"services\": {\n" +
                                            "    \"food\": 200,\n" +
                                            "    \"drink\": 300\n" +
                                            "  },\n" +
                                            "  \"longitude\": 35.2124,\n" +
                                            "  \"latitude\": 31.9026,\n" +
                                            "  \"categories\": {\n" +
                                            "    \"WEDDINGS\": 500,\n" +
                                            "    \"BIRTHDAYS\": 200,\n" +
                                            "    \"MEETINGS\": 100,\n" +
                                            "    \"PARTIES\": 1500,\n" +
                                            "    \"FUNERALS\": 1400\n" +
                                            "  },\n" +
                                            "  \"image\": \"http://localhost:8080/HallImage/1724069778700_venue2.jpg,http://localhost:8080/HallImage/1724069778737_venue1.jpg\",\n" +
                                            "  \"proofFile\": \"http://localhost:8080/proofHalls/123456_proof.pdf\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":500,\"message\":\"Internal server error\"}")))
    })
    @PostMapping("/addHall")
    public HallResponse createHall(@RequestBody Hall hallRequest , HttpServletRequest httpServletRequest) throws  UserNotFoundException {
        String token = authenticationService.extractToken(httpServletRequest);
        User user = authenticationService.extractUserFromToken(token);
        validateLoggedInHallOwner(user);
        return hallService.createHall(hallRequest);
    }
    @PostMapping("/uploadImageToHall")
    public String uploadImage(@RequestPart("images") MultipartFile[] file) throws IOException, UserNotFoundException {
        return hallService.uploadImage(file);
    }

    @PutMapping("/{OwnerId}")
    public HallResponse updateHall(@RequestBody HallDTO hall, @PathVariable Long OwnerId) throws UserNotFoundException {
        return hallService.updateHall(hall,OwnerId);
    }

    @DeleteMapping("/")
    public HallResponse deleteHall(@RequestParam Long id ,@RequestParam Long OwnerId) throws UserNotFoundException {
        return hallService.deleteHall(id,OwnerId);
    }

    @GetMapping("/getAll")
    public PaginationDTO<Hall> getAll(@RequestParam(defaultValue = "") Long ownerId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) throws UserNotFoundException {
        return hallService.getHallsByOwner(page,size,ownerId );
    }

    @GetMapping("/getReservedHalls")
    public PaginationDTO<GetReservationDTO> getReservedHalls(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "") Long ownerId) throws UserNotFoundException {
        return hallService.findReservedHallByOwner(page, size, ownerId);
    }
    @PutMapping("/UpdateProfile/{id}")
    public GeneralResponse updateProfile(@PathVariable Long id, @RequestBody HallOwnerDTO hallOwner) throws UserNotFoundException {
        return authenticationService.UpdateHallOwner(id,hallOwner);
    }

    @GetMapping("getHallOwnerByUserId/{userId}")
    public HallOwner getCustomerByUserId(@PathVariable Long userId) {
        return hallOwnerRepository.getHallOwnerByUserId(userId);
    }
    @GetMapping("/getDeletedHallsByHallOwner")
    PaginationDTO<Hall> getDeletedHallsByHallOwner(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "") Long hallOwnerId) throws UserNotFoundException {
        return hallService.getDeletedHallsByHallOwner(page, size, hallOwnerId);
    }

    @PutMapping("/restoreHall")
    public GeneralResponse restoreHall(@RequestParam Long id, @RequestParam Long ownerId) throws UserNotFoundException {
        return hallService.restoreHall(id, ownerId);
    }

    @PostMapping("/{hallId}/add-image")
    public ResponseEntity<String> addNewImageToHall(@PathVariable Long hallId, @RequestParam("image") MultipartFile[] image) throws IOException {
        String updatedImages = storageService.addNewImage(hallId, image);
        return ResponseEntity.ok("Updated images: " + updatedImages);
    }

    @DeleteMapping("/{hallId}/delete-image")
    public ResponseEntity<String> deleteImageFromHall(@PathVariable Long hallId, @RequestParam("imageUrl") String imageUrl) throws IOException {
        storageService.deleteImage(hallId, imageUrl);
        return ResponseEntity.ok("Image deleted successfully.");
    }

    @GetMapping("/hallsReservationReport/{ownerId}")
    public String getHallsByOwnerId(
            @PathVariable Long ownerId,
            @RequestParam(required = false, defaultValue = "#3498db") String headerColor,
            @RequestParam(required = false, defaultValue = "#ecf0f1") String evenRowColor,
            @RequestParam(required = false, defaultValue = "#ffffff") String oddRowColor
    ) throws UserNotFoundException, DocumentException, IOException {
        return storageService.generateHallReservationReport(ownerId, headerColor, evenRowColor, oddRowColor);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String fileName) throws IOException, UserNotFoundException {
        FileData imageData = storageService.downloadImageFromFileSystem(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(imageData.getType()))
                .body(imageData.getData());
    }

    @PostMapping("/uploadFileProof")
    public ResponseEntity<String> uploadImageToProfile(@RequestPart("file") MultipartFile image) throws UserNotFoundException, IOException {
        String imageUrl =  hallService.uploadHallProve(image);
        return ResponseEntity.ok(imageUrl);
    }
}
