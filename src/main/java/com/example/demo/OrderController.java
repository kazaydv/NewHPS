package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Allows your Render frontend to talk to this backend
public class OrderController {

    @Autowired
    private GoogleSheetsService sheetsService;

    // A simple Map to store OTPs temporarily in memory
    // In a real app, you'd use Redis or a Database, but this works for testing!
    private Map<String, String> otpStorage = new HashMap<>();

    /**
     * STEP 1: Send OTP via Sparrow SMS
     */
    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("phone") String phone) {
        try {
            // Generate a random 6-digit OTP
            String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
            otpStorage.put(phone, otp);

            System.out.println("DEBUG: Sending OTP " + otp + " to " + phone);

            // TODO: Call your Sparrow SMS API here
            // Example: sparrowService.send(phone, "Your OTP is: " + otp);

            return "OTP Sent";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * STEP 2: Verify the OTP entered by user
     */
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("phone") String phone, @RequestParam("otp") String otp) {
        String savedOtp = otpStorage.get(phone);
        if (savedOtp != null && savedOtp.equals(otp)) {
            return "verified";
        }
        return "invalid";
    }

    /**
     * STEP 3: Save Order to Google Sheets
     * Note: We use Multipart to match your 'fdPreview' FormData from HTML
     */
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
            System.out.println("DEBUG: Received order for " + name + " - Total: " + total);

            Order newOrder = new Order(name, phone, address, province, orderPackage, total);
            sheetsService.addOrderToSheet(newOrder);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "failed: " + e.getMessage();
        }
    }
}