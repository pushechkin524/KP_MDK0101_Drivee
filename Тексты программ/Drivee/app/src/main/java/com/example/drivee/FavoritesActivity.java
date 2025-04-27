package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private List<Car> favoriteCars;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        favoriteCars = new ArrayList<>();
        carAdapter = new CarAdapter(this, favoriteCars);
        recyclerView.setAdapter(carAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        TextView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(FavoritesActivity.this, AllActivity.class));
            finish();
        });


        loadFavoriteCars();
    }

    private void loadFavoriteCars() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("favorites")) {
                        List<String> favoriteCarIds = (List<String>) documentSnapshot.get("favorites");
                        if (favoriteCarIds != null && !favoriteCarIds.isEmpty()) {
                            fetchCarsOneByOne(favoriteCarIds);
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FavoritesActivity", "Ошибка загрузки избранного", e);
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void fetchCarsOneByOne(List<String> carIds) {
        favoriteCars.clear();

        for (String carId : carIds) {
            db.collection("cars").document(carId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            long mileage = documentSnapshot.getLong("mileage") != null ? documentSnapshot.getLong("mileage") : 0;

                            Car car = new Car(
                                    documentSnapshot.getId(),
                                    documentSnapshot.getString("brand"),
                                    documentSnapshot.getString("model"),
                                    documentSnapshot.getString("photoone"),
                                    mileage,
                                    documentSnapshot.getLong("price")
                            );

                            car.setFavorite(true);
                            favoriteCars.add(car);
                            carAdapter.notifyDataSetChanged();
                        }
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FavoritesActivity", "Ошибка загрузки машины", e);
                        progressBar.setVisibility(View.GONE);
                    });
        }
    }
}
