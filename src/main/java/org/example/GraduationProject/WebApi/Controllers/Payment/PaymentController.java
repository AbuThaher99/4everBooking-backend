package org.example.GraduationProject.WebApi.Controllers.Payment;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.GraduationProject.Core.Servecies.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;


    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent(@RequestBody Map<String, Object> paymentDetails) {
        try {
            // Safely cast the amount to Long
            long amount = ((Number) paymentDetails.get("amount")).longValue();
            String currency = (String) paymentDetails.get("currency");
            String connectedAccountId = (String) paymentDetails.get("connectedAccountId"); // Hall owner's account ID
            double platformFeePercentage = 0.1; // Platform fee percentage (10%)

            // Delegate to payment service to handle the creation logic
            return paymentService.createPaymentIntent(amount, currency, connectedAccountId, platformFeePercentage);

        } catch (ClassCastException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid input type: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Payment creation failed: " + e.getMessage());
            return errorResponse;
        }
    }

    @Operation(summary = "Resend onboarding link", description = "Resends the onboarding link for a specific connected account if the onboarding process is incomplete.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Onboarding link resent successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"onboardingLink\": \"https://connect.stripe.com/setup/s/example-onboarding-link\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid input or request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Failed to resend onboarding link: <error details>\"}")))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Input payload with the connected account ID", required = true, content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = "{\"connectedAccountId\": \"acct_1A2B3C4D5E6F\"}")))
    @PostMapping("/resend-onboarding-link")
    public Map<String, String> resendOnboardingLink(@RequestBody Map<String, Object> requestBody) {
        try {
            String connectedAccountId = (String) requestBody.get("connectedAccountId");
            String onboardingLink = paymentService.resendOnboardingLink(connectedAccountId);

            Map<String, String> response = new HashMap<>();
            response.put("onboardingLink", onboardingLink);
            return response;
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to resend onboarding link: " + e.getMessage());
            return errorResponse;
        }
    }
}
