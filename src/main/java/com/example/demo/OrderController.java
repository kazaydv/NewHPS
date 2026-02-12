package com.example.demo; // This must match your screenshot!

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

@RestController
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private GoogleSheetsService sheetsService;

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
            e.printStackTrace();
            return "failed: " + e.getMessage();
        }
    }
}