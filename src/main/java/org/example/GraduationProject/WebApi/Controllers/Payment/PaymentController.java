package org.example.GraduationProject.WebApi.Controllers.Payment;


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

            return paymentService.createPaymentIntent(amount, currency);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }
}
