package com.example.demo;

public class Order {
    private String name;
    private String phone;
    private String address;
    private String province;
    private String packageType;
    private String total;

    public Order(String name, String phone, String address, String province, String packageType, String total) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.province = province;
        this.packageType = packageType;
        this.total = total;
    }

    // Getters
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getProvince() { return province; }
    public String getPackageType() { return packageType; }
    public String getTotal() { return total; }
}