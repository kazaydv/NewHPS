package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@CrossOrigin(origins = "*")
public class OtpController {

    @Value("${sparrow.token}")
    private String SPARROW_TOKEN;

    @Value("${sparrow.sender}")
    private String SENDER_ID;

    private Map<String, OtpData> otpStorage = new HashMap<>();

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("phone") String phone) {

        if (phone == null || phone.isEmpty()) {
            return "Phone number is required";
        }

        // Generate 6 digit OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStorage.put(phone, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));

        try {
            // FIX: URLEncoder handles the spaces in "Your Easy Spray OTP is"
            // Without this, the URL becomes invalid and throws an Exception.
            String message = "Your Easy Spray OTP is " + otp;
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

            // Construct the URL using the encoded message
            String url = "https://api.sparrowsms.com/v2/sms/?token=" + SPARROW_TOKEN
                    + "&from=" + SENDER_ID
                    + "&to=" + phone
                    + "&text=" + encodedMessage;

            RestTemplate restTemplate = new RestTemplate();

            // Log the request for your own debugging (check IntelliJ console)
            System.out.println("Requesting Sparrow API: " + url);

            String response = restTemplate.getForObject(url, String.class);

            // Log the actual response from Sparrow (it will tell you if the token/IP is wrong)
            System.out.println("Sparrow API Response: " + response);

            return "OTP Sent";
        } catch (Exception e) {
            // This prints the actual error (like 401 Unauthorized) to your IDE console
            e.printStackTrace();
            return "Failed to send OTP: " + e.getMessage();
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("phone") String phone, @RequestParam("otp") String otp) {
        if (!otpStorage.containsKey(phone)) return "OTP not sent";

        OtpData stored = otpStorage.get(phone);
        if (LocalDateTime.now().isAfter(stored.expiry)) {
            otpStorage.remove(phone);
            return "OTP expired";
        }

        if (stored.otp.equals(otp)) {
            otpStorage.remove(phone);
            return "verified";
        }
        return "Invalid OTP";
    }

    static class OtpData {
        String otp;
        LocalDateTime expiry;
        OtpData(String otp, LocalDateTime expiry) { this.otp = otp; this.expiry = expiry; }
    }
}