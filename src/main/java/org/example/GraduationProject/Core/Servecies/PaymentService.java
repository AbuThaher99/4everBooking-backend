package org.example.GraduationProject.Core.Servecies;


import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    // Set your Stripe secret key directly
    private final String stripeSecretKey = "sk_test_51QXoX8DyvCG9R2BppagaaHjfKE5Z89E6djFXnFlTCqBn82JvEipJXnLVQlozwNaUdybf7LyFEvfeGqcw8ndfK7jp00QyfMw40k";

    public PaymentService() {
        Stripe.apiKey = stripeSecretKey; // Set the Stripe API key
    }

    public Map<String, String> createPaymentIntent(long amount, String currency) throws Exception {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount) // Amount in cents
                .setCurrency(currency)
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        return response;
    }
}
