package com.example.drivee;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Address;
import android.location.Geocoder;
import java.util.Locale;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateListingActivity extends AppCompatActivity {

    private List<ImageView> imagePreviews = new ArrayList<>();
    private EditText editVin, editBrand, editModel, editYear, editColor, editVolume, editHorsepower,
            editMileage, editOwners, editPrice, editDescription, editCity;
    private Spinner spinnerDrive, spinnerTransmission, spinnerFuel, spinnerBody;
    private Button btnCreateListing, btnUploadPhoto, btnResetPhotos;
    private final List<Uri> selectedImages = new ArrayList<>();

    private static final int MAX_PHOTOS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_listing);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getCityFromLocation();
        }

        imagePreviews.add(findViewById(R.id.imagePreview1));
        imagePreviews.add(findViewById(R.id.imagePreview2));
        imagePreviews.add(findViewById(R.id.imagePreview3));
        imagePreviews.add(findViewById(R.id.imagePreview4));
        imagePreviews.add(findViewById(R.id.imagePreview5));

        editVin = findViewById(R.id.editVin);
        editBrand = findViewById(R.id.editBrand);
        editModel = findViewById(R.id.editModel);
        editYear = findViewById(R.id.editYear);
        editColor = findViewById(R.id.editColor);
        editVolume = findViewById(R.id.editVolume);
        editHorsepower = findViewById(R.id.editHorsepower);
        editMileage = findViewById(R.id.editMileage);
        editOwners = findViewById(R.id.editOwners);
        editPrice = findViewById(R.id.editPrice);
        editDescription = findViewById(R.id.editDescription);
        editCity = findViewById(R.id.editCity);

        spinnerDrive = findViewById(R.id.spinnerDrive);
        spinnerTransmission = findViewById(R.id.spinnerTransmission);
        spinnerFuel = findViewById(R.id.spinnerFuel);
        spinnerBody = findViewById(R.id.spinnerBody);

        btnCreateListing = findViewById(R.id.btnCreateListing);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnResetPhotos = findViewById(R.id.btnRemovePhoto5);

        String selectedBrand = getIntent().getStringExtra("selectedBrand");
        String selectedModel = getIntent().getStringExtra("selectedModel");

        if (selectedBrand != null && !selectedBrand.isEmpty()) {
            editBrand.setText(selectedBrand);
            editBrand.setEnabled(false);
        }

        if (selectedModel != null && !selectedModel.isEmpty()) {
            editModel.setText(selectedModel);
            editModel.setEnabled(false);
        }

        setupSpinners();

        btnUploadPhoto.setOnClickListener(v -> openGallery());
        btnCreateListing.setOnClickListener(v -> saveListing());
        btnResetPhotos.setOnClickListener(v -> resetPhotos());
    }

    private void getCityFromLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        String city = addresses.get(0).getLocality();
                        if (city != null) {
                            editCity.setText(city);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCityFromLocation();
        }
    }


    private void setupSpinners() {
        String[] driveOptions = {"Привод", "Полный", "Передний", "Задний"};
        String[] transmissionOptions = {"Коробка", "Автоматическая", "Механика", "Вариатор", "Робот"};
        String[] fuelOptions = {"Топливо", "Бензин", "Дизель", "Гибрид", "Электро", "ГБО", "Метан"};
        String[] bodyOptions = {"Тип кузова", "Купе", "Седан", "Хэтчбек", "Кроссовер", "Внедорожник"};

        spinnerDrive.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, driveOptions));
        spinnerTransmission.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, transmissionOptions));
        spinnerFuel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fuelOptions));
        spinnerBody.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bodyOptions));
    }

    private void openGallery() {
        if (selectedImages.size() < MAX_PHOTOS) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && selectedImages.size() < MAX_PHOTOS; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    selectedImages.add(imageUri);
                    imagePreviews.get(i).setImageURI(imageUri);
                }
            } else if (data.getData() != null && selectedImages.size() < MAX_PHOTOS) {
                Uri imageUri = data.getData();
                selectedImages.add(imageUri);
                imagePreviews.get(0).setImageURI(imageUri);
            }
        }
    }

    private void resetPhotos() {
        selectedImages.clear();
        for (ImageView imageView : imagePreviews) {
            imageView.setImageResource(android.R.color.darker_gray);
        }
    }

    private void saveListing() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (spinnerDrive.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Выберите тип привода", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerTransmission.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Выберите тип коробки", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerFuel.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Выберите тип топлива", Toast.LENGTH_SHORT).show();
            return;
        }
        if (spinnerBody.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Выберите тип кузова", Toast.LENGTH_SHORT).show();
            return;
        }

        String vin = editVin.getText().toString().trim();
        if (vin.length() != 17) {
            Toast.makeText(this, "VIN должен содержать 17 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        int year = parseIntSafe(editYear.getText().toString());
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

        if (year < 1850 || year > currentYear) {
            Toast.makeText(this, "Год выпуска должен быть между 1850 и " + currentYear, Toast.LENGTH_SHORT).show();
            return;
        }

        String color = editColor.getText().toString().trim();
        if (!color.matches("^[А-Яа-яA-Za-z\\s]+$")) {
            Toast.makeText(this, "Цвет должен содержать только буквы", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> carData = new HashMap<>();
        carData.put("vin", vin);
        carData.put("brand", editBrand.getText().toString().trim());
        carData.put("model", editModel.getText().toString().trim());
        carData.put("year", year);
        carData.put("ownerId", userId);
        carData.put("color", editColor.getText().toString().trim());
        carData.put("volume", parseIntSafe(editVolume.getText().toString()));
        carData.put("horsepower", parseIntSafe(editHorsepower.getText().toString()));
        carData.put("mileage", parseIntSafe(editMileage.getText().toString()));
        carData.put("owners", parseIntSafe(editOwners.getText().toString()));
        carData.put("price", parseIntSafe(editPrice.getText().toString()));
        carData.put("description", editDescription.getText().toString().trim());
        carData.put("city", editCity.getText().toString().trim());
        carData.put("drive", spinnerDrive.getSelectedItem().toString());
        carData.put("transmission", spinnerTransmission.getSelectedItem().toString());
        carData.put("fuel", spinnerFuel.getSelectedItem().toString());
        carData.put("body", spinnerBody.getSelectedItem().toString());
        carData.put("status", "pending");

        for (int i = 0; i < 5; i++) {
            String keyName = "photo" + (i == 0 ? "one" :
                    i == 1 ? "two" :
                            i == 2 ? "three" :
                                    i == 3 ? "four" : "five");

            if (i < selectedImages.size()) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImages.get(i));
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                    byte[] imageBytes = outputStream.toByteArray();
                    String base64 = Base64.encodeToString(imageBytes, Base64.DEFAULT); // Кодируем изображение в Base64
                    carData.put(keyName, base64); // Добавляем в коллекцию Firestore
                } catch (Exception e) {
                    e.printStackTrace();
                    carData.put(keyName, "");
                }
            } else {
                carData.put(keyName, "");
            }
        }

        db.collection("cars")
                .add(carData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Объявление добавлено и ожидает модерации!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateListingActivity.this, AllActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
