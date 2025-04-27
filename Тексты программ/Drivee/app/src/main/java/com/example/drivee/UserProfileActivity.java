package com.example.drivee;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private Button selectImageBtn;
    private TextView userName, userPhone, userEmail;
    private RecyclerView userCarsRecyclerView, userJournalRecyclerView;

    private List<Car> userCars = new ArrayList<>();
    private List<BortjournalPost> userJournalPosts = new ArrayList<>();

    private CarAdapter carAdapter;
    private BortjournalAdapter bortjournalAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Uri imageUri;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profileImage = findViewById(R.id.profileImage);
        selectImageBtn = findViewById(R.id.selectImageBtn);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        userEmail = findViewById(R.id.userEmail);
        userCarsRecyclerView = findViewById(R.id.userCarsRecyclerView);
        userJournalRecyclerView = findViewById(R.id.userJournalRecyclerView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        carAdapter = new CarAdapter(this, userCars, false, true);
        userCarsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userCarsRecyclerView.setAdapter(carAdapter);

        bortjournalAdapter = new BortjournalAdapter(this, userJournalPosts, true);
        userJournalRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userJournalRecyclerView.setAdapter(bortjournalAdapter);

        selectImageBtn.setOnClickListener(v -> openFileChooser());

        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadUserProfile();
        loadUserCars();
        loadUserJournalPosts();
    }

    private void loadUserProfile() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userName.setText(documentSnapshot.getString("name"));
                        userPhone.setText(documentSnapshot.getString("phone"));
                        userEmail.setText(documentSnapshot.getString("email"));

                        String profileImageBase64 = documentSnapshot.getString("profile_image");
                        if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                            Bitmap decodedImage = decodeBase64(profileImageBase64);
                            profileImage.setImageBitmap(decodedImage);
                        } else {
                            profileImage.setImageResource(R.drawable.logo);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserCars() {
        db.collection("cars")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userCars.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String ownerId = document.getString("ownerId");
                        if (ownerId != null && ownerId.equals(userId)) {
                            String id = document.getId();
                            String brand = document.getString("brand");
                            String model = document.getString("model");
                            String photo = document.getString("photoone");
                            if (photo == null) photo = "";
                            long price = document.getLong("price") != null ? document.getLong("price") : 0;
                            long mileage = document.getLong("mileage") != null ? document.getLong("mileage") : 0;
                            String status = document.getString("status") != null ? document.getString("status") : "Неизвестно";

                            Car car = new Car(id, brand, model, photo, mileage, price);
                            try {
                                java.lang.reflect.Field field = car.getClass().getDeclaredField("status");
                                field.setAccessible(true);
                                field.set(car, status);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            userCars.add(car);
                        }
                    }
                    carAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки машин", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserJournalPosts() {
        db.collection("bortjornal")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userJournalPosts.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        BortjournalPost post = doc.toObject(BortjournalPost.class);
                        post.setId(doc.getId());
                        userJournalPosts.add(post);
                    }
                    bortjournalAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки журнала", Toast.LENGTH_SHORT).show();
                });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                profileImage.setImageBitmap(selectedImage);

                String imageBase64 = encodeImageToBase64(selectedImage);
                saveImageToFirestore(imageBase64);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap decodeBase64(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void saveImageToFirestore(String imageBase64) {
        db.collection("users").document(userId)
                .update("profile_image", imageBase64)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Изображение обновлено", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка обновления изображения", Toast.LENGTH_SHORT).show();
                });
    }
}
