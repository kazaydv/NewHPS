package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller; // Changed from RestController
import org.springframework.ui.Model; // Added for Meta Pixel data
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller // This must be @Controller to render HTML pages
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private GoogleSheetsService sheetsService;

    @Value("${sparrow.token}")
    private String SPARROW_TOKEN;

    @Value("${sparrow.sender}")
    private String SENDER_ID;

    private Map<String, String> otpStorage = new HashMap<>();

    // We use @ResponseBody here so this specific method still returns text (for your AJAX)
    @PostMapping("/send-otp")
    @ResponseBody
    public String sendOtp(@RequestParam("phone") String phone) {
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        otpStorage.put(phone, otp);

        try {
            String message = "Your confirmation code for Herbal piles spray is " + otp;
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

            String url = "https://api.sparrowsms.com/v2/sms/?token=" + SPARROW_TOKEN
                    + "&from=" + SENDER_ID
                    + "&to=" + phone
                    + "&text=" + encodedMessage;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            // Log this so you can see if Sparrow gives a 1002 error in Render logs
            System.out.println("Sparrow SMS Response: " + response);

            return "OTP Sent";
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public String verifyOtp(@RequestParam("phone") String phone, @RequestParam("otp") String otp) {
        if (otp.equals(otpStorage.get(phone))) return "verified";
        return "invalid";
    }

    @PostMapping(value = "/order", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String placeOrder(
            @RequestParam("name") String name,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("province") String province,
            @RequestParam("package") String orderPackage,
            @RequestParam("total") String total,
            Model model
    ) {
        try {
            // 1. Save to Google Sheets
            Order newOrder = new Order(name, phone, address, province, orderPackage, total);
            sheetsService.addOrderToSheet(newOrder);

            // 2. Prepare Data for Meta Pixel "Purchase" Event
            // Cleans "NPR 2500" into "2500" so the pixel can read it
            String numericTotal = total.replaceAll("[^0-9]", "");
            model.addAttribute("purchaseValue", numericTotal);
            model.addAttribute("currency", "NPR");

            // 3. This returns success.html from /templates/
            return "success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}