package com.example.drivee;

import android.util.Log;
import com.google.android.gms.common.api.internal.IStatusCallback;


public class Car {
    private String id;
    private String brand;
    private String model;
    private String photoBase64;
    private long mileage;
    private long price;
    private boolean isFavorite;
    private String status;

    // Конструктор
    public Car(String id, String brand, String model, String photoBase64, long mileage, long price) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.photoBase64 = photoBase64;
        this.mileage = mileage;
        this.price = price;
    }

    public String getId() { return id; }

    public String getBrand() { return brand; }

    public String getModel() { return model; }

    public String getPhotoBase64() { return photoBase64; }

    public long getMileage() { return mileage; }

    public long getPrice() { return price; }

    public boolean isFavorite() { return isFavorite; }

    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}



