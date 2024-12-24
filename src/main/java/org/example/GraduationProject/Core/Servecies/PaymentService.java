package org.example.GraduationProject.Core.Servecies;


import com.stripe.Stripe;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.PaymentIntent;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.example.GraduationProject.Common.Entities.HallOwner;
import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Core.Repsitories.HallOwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    // Set your Stripe secret key directly
    private final String stripeSecretKey = "sk_test_51PluNHFNc7gJPoUcJoku6Wo1GRFadIGrbmTFr12gLXinEx4KWwQDxDrfbIulbJQdpETS9fxTJD5yJTKXty6RpdMg00DSDLs0cu";

    @Autowired
    private  HallOwnerRepository hallOwnerRepository;
    public PaymentService() {
        Stripe.apiKey = stripeSecretKey; // Set the Stripe API key
    }

    public Map<String, String> createPaymentIntent(long amount, String currency, String connectedAccountId, double platformFeePercentage) throws Exception {
        long applicationFee = (long) (amount * platformFeePercentage);

        PaymentIntentCreateParams.TransferData transferData = PaymentIntentCreateParams.TransferData.builder()
                .setDestination(connectedAccountId)
                .build();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .addPaymentMethodType("card")
                .setTransferData(transferData) // Transfer funds to connected account
                .setApplicationFeeAmount(applicationFee) // Platform fee
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        // Debugging logs
        System.out.println("PaymentIntent created: ID = " + intent.getId() + ", ClientSecret = " + intent.getClientSecret());

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        return response;
    }


    public String createConnectedAccount(Long user) {
        HallOwner hall = hallOwnerRepository.getHallOwnerByUserId(user);
        Date dob = hall.getUser().getDateOfBirth();
        if (dob == null) {
            throw new RuntimeException("Date of birth is null");
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(dob);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        int month = calendar.get(java.util.Calendar.MONTH) + 1; // Months are 0-based
        int year = calendar.get(java.util.Calendar.YEAR);
        System.out.println("day: " + day + ", month: " + month + ", year: " + year);

        try {
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS) // Use "STANDARD" for standard accounts
                    .setCountry("US") // Replace with the owner's country
                    .setEmail(hall.getUser().getEmail()) // Owner's email
                    .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL) // Set business type
                    .setIndividual(AccountCreateParams.Individual.builder()
                            .setFirstName(hall.getUser().getFirstName())
                            .setLastName(hall.getUser().getLastName())
                            .setDob(AccountCreateParams.Individual.Dob.builder()
                                    .setDay((long) day)
                                    .setMonth((long) month)
                                    .setYear((long) year)
                                    .build())
                            .setAddress(AccountCreateParams.Individual.Address.builder()
                                    .setLine1(hall.getUser().getAddress())
                                    .setCity(hall.getUser().getAddress())
                                    .setPostalCode("10001")
                                    .setCountry("US")
                                    .build())
                            .build())
                    .build();

            Account account = Account.create(params);
            hall.setConnectedAccountId(account.getId());
            hallOwnerRepository.save(hall);

            // Create an onboarding link for the user
            String onboardingUrl = createAccountLink(account.getId());
            System.out.println("Onboarding URL: " + onboardingUrl);
           return onboardingUrl;
        } catch (Exception e) {
            throw new RuntimeException("Error creating connected account: " + e.getMessage());
        }
    }

    private String createAccountLink(String connectedAccountId) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(connectedAccountId)
                    .setRefreshUrl("https://example.com/reauth")
                    .setReturnUrl("https://example.com/success")
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink accountLink = AccountLink.create(params);
            return accountLink.getUrl(); // Send this link to the user
        } catch (Exception e) {
            throw new RuntimeException("Error creating account link: " + e.getMessage());
        }
    }

    public boolean isOnboardingIncomplete(String connectedAccountId) {
        try {
            // Retrieve the connected account
            Account account = Account.retrieve(connectedAccountId);

            // Check if there are any requirements that are currently due
            if (!account.getRequirements().getCurrentlyDue().isEmpty()) {
                System.out.println("Onboarding is incomplete for account: " + connectedAccountId);
                System.out.println("Fields currently due: " + account.getRequirements().getCurrentlyDue());
                return true; // Onboarding is incomplete
            }

            System.out.println("Onboarding is complete for account: " + connectedAccountId);
            return false; // Onboarding is complete
        } catch (Exception e) {
            throw new RuntimeException("Error checking account status: " + e.getMessage());
        }
    }

    public String resendOnboardingLink(String connectedAccountId) {
        try {
            // Check if onboarding is incomplete
            if (isOnboardingIncomplete(connectedAccountId)) {
                AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                        .setAccount(connectedAccountId)
                        .setRefreshUrl("https://example.com/reauth")
                        .setReturnUrl("https://example.com/success")
                        .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                        .build();

                AccountLink accountLink = AccountLink.create(params);
                System.out.println("Resent onboarding URL: " + accountLink.getUrl());
                return accountLink.getUrl(); // Send this link to the user
            } else {
                return "Onboarding is already complete.";
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resending onboarding link: " + e.getMessage());
        }
    }

}
