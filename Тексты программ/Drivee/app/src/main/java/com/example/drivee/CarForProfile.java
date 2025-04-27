package com.example.drivee;

public class CarForProfile {
    private String id;
    private String brand;
    private String model;
    private long price;
    private String status;

    public CarForProfile(String id, String brand, String model, long price, String status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.price = price;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
