package com.example.drivee;

public class UserCar {
    private String id;
    private String brand;
    private String model;
    private String photoUrl;
    private long price;
    private String status;

    public UserCar(String id, String brand, String model, String photoUrl, long price, String status) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.photoUrl = photoUrl;
        this.price = price;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public long getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
