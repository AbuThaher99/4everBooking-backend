package org.example.GraduationProject.WebApi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    private GoogleCredentials googleCredentials; // Store credentials globally

    @PostConstruct
    public void initializeFirebase() throws IOException {
        String base64Credentials = System.getenv("FIREBASE_CONFIG");

        if (base64Credentials == null) {
            throw new IOException("FIREBASE_CONFIG environment variable not set");
        }

        // Decode base64 credentials
        byte[] decodedCredentials = Base64.getDecoder().decode(base64Credentials);
        ByteArrayInputStream credentialsStream = new ByteArrayInputStream(decodedCredentials);

        // Initialize GoogleCredentials
        this.googleCredentials = GoogleCredentials.fromStream(credentialsStream);

        // Initialize Firebase
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(this.googleCredentials)
                .setStorageBucket("graduationproject-df4b7.appspot.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        System.out.println("Firebase initialized with Storage bucket: " + FirebaseApp.getInstance().getOptions().getStorageBucket());

        // Call the CORS configuration setup
        setCorsConfiguration(decodedCredentials); // Pass the credentials
    }

    public void setCorsConfiguration(byte[] credentialsBytes) {
        try {
            // Use the same credentials for Google Cloud Storage
            ByteArrayInputStream credentialsStream = new ByteArrayInputStream(credentialsBytes);
            GoogleCredentials storageCredentials = GoogleCredentials.fromStream(credentialsStream);

            // Initialize Google Cloud Storage
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(storageCredentials)
                    .build()
                    .getService();

            String bucketName = "graduationproject-df4b7.appspot.com"; // Replace with your bucket name
            Bucket bucket = storage.get(bucketName);

            // Define CORS rules
            Cors cors = Cors.newBuilder()
                    .setOrigins(
                            Arrays.asList(
                                    Cors.Origin.of("http://localhost:3000"),
                                    Cors.Origin.of("http://127.0.0.1:3000"),
                                    Cors.Origin.of("https://website-4everbooking-frontend-4b9d04424128.herokuapp.com")
                            )
                    )
                    .setMethods(Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS)) // Correct type
                    .setResponseHeaders(Arrays.asList("Content-Type", "Authorization"))
                    .setMaxAgeSeconds(3600)
                    .build();

            // Apply the CORS configuration to the bucket
            bucket.toBuilder().setCors(Arrays.asList(cors)).build().update();

            System.out.println("CORS configuration has been updated for bucket: " + bucketName);
        } catch (Exception e) {
            System.err.println("Failed to set CORS configuration: " + e.getMessage());
        }
    }
}