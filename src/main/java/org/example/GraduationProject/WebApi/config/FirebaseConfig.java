package org.example.GraduationProject.WebApi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {
        String base64Credentials = System.getenv("FIREBASE_CONFIG");

        if (base64Credentials == null) {
            throw new IOException("FIREBASE_CONFIG environment variable not set");
        }
        byte[] decodedCredentials = Base64.getDecoder().decode(base64Credentials);
        ByteArrayInputStream credentialsStream = new ByteArrayInputStream(decodedCredentials);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .setStorageBucket("graduationproject-df4b7.appspot.com")
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        System.out.println("Firebase initialized with Storage bucket: " + FirebaseApp.getInstance().getOptions().getStorageBucket());
    }
}
