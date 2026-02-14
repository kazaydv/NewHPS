package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private GoogleSheetsService sheetsService;

    @Value("${sparrow.token}")
    private String SPARROW_TOKEN;

    @Value("${sparrow.sender}")
    private String SENDER_ID;

    private Map<String, String> otpStorage = new HashMap<>();

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("phone") String phone) {
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        otpStorage.put(phone, otp);

        try {
            String message = "Your confirmation code for Herbal piles spray is " + otp;
            // Use .name() to satisfy the IntelliJ warning
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.name());

            String url = "https://api.sparrowsms.com/v2/sms/?token=" + SPARROW_TOKEN
                    + "&from=" + SENDER_ID
                    + "&to=" + phone
                    + "&text=" + encodedMessage;

            new RestTemplate().getForObject(url, String.class);
            return "OTP Sent";
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("phone") String phone, @RequestParam("otp") String otp) {
        if (otp.equals(otpStorage.get(phone))) return "verified";
        return "invalid";
    }

    @PostMapping(value = "/Order", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String placeOrder(
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("province") String province,
            @RequestParam("package") String orderPackage,
            @RequestParam("total") String total
    ) {
        try {
            Order newOrder = new Order(name, phone, address, province, orderPackage, total);
            sheetsService.addOrderToSheet(newOrder);
            return "success";
        } catch (Exception e) {
            return "failed: " + e.getMessage();
        }
    }
}
