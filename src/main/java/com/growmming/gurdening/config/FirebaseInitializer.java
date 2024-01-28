//package com.growmming.gurdening.config;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.auth.FirebaseAuth;
//import java.io.FileInputStream;
//import java.io.IOException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Slf4j
//@Configuration
//public class FirebaseInitializer {
//
//    @Bean
//    public FirebaseApp firebaseApp() throws IOException {
//        log.info("Initializing Firebase.");
//        FileInputStream firebaseKey =
//                new FileInputStream("src/main/resources/gurdening-firebase-login.json");
//
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(GoogleCredentials.fromStream(firebaseKey))
//                .build();
//
//        FirebaseApp app = FirebaseApp.initializeApp(options);
//        log.info("FirebaseApp initialized" + app.getName());
//        return app;
//    }
//
//    @Bean
//    public FirebaseAuth getFirebaseAuth() throws IOException {
//        return FirebaseAuth.getInstance(firebaseApp());
//    }
//}
