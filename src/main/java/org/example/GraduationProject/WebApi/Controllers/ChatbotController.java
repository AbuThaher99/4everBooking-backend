package org.example.GraduationProject.WebApi.Controllers;

import org.example.GraduationProject.Core.Servecies.HallService;
import org.example.GraduationProject.Core.Servecies.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    private final OpenAIService openAIService;
    private final HallService hallService;

    @Autowired
    public ChatbotController(OpenAIService openAIService, HallService hallService) {
        this.openAIService = openAIService;
        this.hallService = hallService;
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> handleUserQuery(@RequestBody Map<String, String> request) {
        String userInput = request.get("query");
        String responseMessage;
        System.out.println("User input: " + userInput);

        // Check if the user is asking about hall capacity
        if ((userInput.contains("capacity") || userInput.contains("سعة")) &&
                (userInput.contains("hall") || userInput.contains("قاعة") || userInput.contains("قاعات"))) {

            // Extract the capacity value using regex
            Integer capacity = extractCapacity(userInput);
            System.out.println("Extracted capacity: " + capacity);

            if (capacity != null) {
                // Check if the input is in Arabic
                boolean isArabic = isArabicText(userInput);
                responseMessage = hallService.getHallsbyCapacity(capacity, isArabic);
                System.out.println("Response from HallService: " + responseMessage);
            } else {
                responseMessage = isArabicText(userInput) ?
                        "لم أتمكن من تحديد السعة المطلوبة. يرجى إعادة المحاولة مع ذكر السعة كرقم." :
                        "I couldn't determine the requested capacity. Please try again with a numerical capacity.";
            }
        } else {
            // Get a response from the fine-tuned OpenAI model
            responseMessage = openAIService.getChatbotResponse(userInput);
        }

        Map<String, String> response = new HashMap<>();
        response.put("response", responseMessage);

        return ResponseEntity.ok(response);
    }


    private Integer extractCapacity(String userInput) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(userInput);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return null;
    }

    private boolean isArabicText(String text) {
        return text.matches(".*[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF].*");
    }
}
