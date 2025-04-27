package com.example.drivee;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CenterActivity extends AppCompatActivity {

    private static final String TAG = "CenterActivity";
    private ViewPager2 carImagePager;
    private ImageView userAvatar;
    private TextView userName, userPhone;

    private TextView vinTextView, dataPublicTextView, bodyTextView, brandTextView, colorTextView, descriptionTextView;
    private TextView driveTextView, fuelTextView, horsepowerTextView, mileageTextView, modelTextView;
    private TextView ownersTextView, priceTextView, transmissionTextView, volumeTextView, yearTextView;

    private FirebaseFirestore db;
    private String carId;
    private ImagePagerAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center);

        carId = getIntent().getStringExtra("carId");
        if (carId == null || carId.isEmpty()) {
            Log.e(TAG, "Ошибка: carId не передан!");
            finish();
            return;
        }

        vinTextView = findViewById(R.id.vinTextView);
        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        bodyTextView = findViewById(R.id.bodyTextView);
        brandTextView = findViewById(R.id.brandTextView);
        colorTextView = findViewById(R.id.colorTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        driveTextView = findViewById(R.id.driveTextView);
        fuelTextView = findViewById(R.id.fuelTextView);
        horsepowerTextView = findViewById(R.id.horsepowerTextView);
        mileageTextView = findViewById(R.id.mileageTextView);
        modelTextView = findViewById(R.id.modelTextView);
        carImagePager = findViewById(R.id.carImagePager);
        dataPublicTextView = findViewById(R.id.dataPublicTextView);
        ownersTextView = findViewById(R.id.ownersTextView);
        priceTextView = findViewById(R.id.priceTextView);
        transmissionTextView = findViewById(R.id.transmissionTextView);
        volumeTextView = findViewById(R.id.volumeTextView);
        yearTextView = findViewById(R.id.yearTextView);

        db = FirebaseFirestore.getInstance();

        List<String> initialPhotoUrls = new ArrayList<>();
        imageAdapter = new ImagePagerAdapter(this, initialPhotoUrls);
        carImagePager.setAdapter(imageAdapter);

        loadCarDetails();
        TextView phoneTextView = findViewById(R.id.userPhone);
        phoneTextView.setOnClickListener(v -> {
            String phoneNumber = phoneTextView.getText().toString().trim();
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));

            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, 1);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение получено, нажмите еще раз для звонка", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Разрешение на звонок не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadCarDetails() {
        DocumentReference docRef = db.collection("cars").document(carId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    vinTextView.setText("VIN: " + document.getString("vin"));
                    bodyTextView.setText("Кузов: " + document.getString("body"));
                    brandTextView.setText(document.getString("brand"));
                    colorTextView.setText("Цвет: " + document.getString("color"));
                    descriptionTextView.setText(document.getString("description"));
                    driveTextView.setText("Привод: " + document.getString("drive"));

                    Timestamp timestamp = document.getTimestamp("DataPublic");
                    if (timestamp != null) {
                        Date date = timestamp.toDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                        dataPublicTextView.setText(sdf.format(date));
                    }

                    fuelTextView.setText("Топливо: " + document.getString("fuel"));
                    horsepowerTextView.setText("Мощность: " + document.getLong("horsepower") + " л.с.");
                    mileageTextView.setText("Пробег: " + document.getLong("mileage") + " км");
                    modelTextView.setText(document.getString("model"));
                    ownersTextView.setText("Количество владельцев: " + document.getLong("owners"));
                    priceTextView.setText(document.getLong("price") + " ₽");
                    transmissionTextView.setText("Трансмиссия: " + document.getString("transmission"));
                    volumeTextView.setText("Объем двигателя: " + document.get("volume") + " л");
                    yearTextView.setText(String.valueOf(document.getLong("year")));

                    List<String> photoBase64List = new ArrayList<>();
                    String[] photoFields = {"photoone", "phototwo", "photothree", "photofour", "photofive"};
                    for (String field : photoFields) {
                        String base64 = document.getString(field);
                        if (base64 != null && !base64.isEmpty()) {
                            photoBase64List.add(base64);
                        }
                    }
                    String ownerId = document.getString("ownerId");
                    if (ownerId != null) {
                        db.collection("users").document(ownerId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        userName.setText(userDoc.getString("name"));
                                        userPhone.setText(userDoc.getString("phone"));

                                        String profileBase64 = userDoc.getString("profile_image");
                                        if (profileBase64 != null && !profileBase64.isEmpty()) {
                                            byte[] decodedBytes = Base64.decode(profileBase64, Base64.DEFAULT);
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                            userAvatar.setImageBitmap(bitmap);
                                        } else {
                                            userAvatar.setImageResource(R.drawable.user_prof_1);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Ошибка загрузки данных пользователя", e));
                    }
                    imageAdapter.updateImages(photoBase64List);
                } else {
                    Log.e(TAG, "Документ не найден!");
                }
            } else {
                Log.e(TAG, "Ошибка при загрузке данных", task.getException());
            }
        });
    }
}
