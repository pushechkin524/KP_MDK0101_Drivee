package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminCarAdapter adminCarAdapter;
    private List<Car> carList;
    private BottomNavigationView bottomNavigationView;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        carList = new ArrayList<>();
        adminCarAdapter = new AdminCarAdapter(this, carList);
        recyclerView.setAdapter(adminCarAdapter);

        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_manage_models) {
                Intent intent = new Intent(AdminActivity.this, ManageModelsActivity.class);
                startActivity(intent);
                return true;
            }
            else if (item.getItemId() == R.id.nav_manage_bort) {
                Intent intent = new Intent(AdminActivity.this, AdminBortjournalActivity.class);
                startActivity(intent);
                return true;
            }
            else if (item.getItemId() == R.id.nav_manage_obj) {
                Intent intent = new Intent(AdminActivity.this, AdminActivity.class);
                startActivity(intent);
                return true;
            }
            else if (item.getItemId() == R.id.exit) {
                FirebaseAuth.getInstance().signOut(); // Обязательно выйти из аккаунта
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Очищаем стек
                startActivity(intent);
                return true;
            }

            return false;
        });

        loadPendingCars();
    }




    private void loadPendingCars() {
        db.collection("cars")
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        carList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String photoone = document.getString("photoone");
                            if (photoone == null) photoone = "";
                            long mileage = document.getLong("mileage") != null ? document.getLong("mileage") : 0;

                            Car car = new Car(
                                    document.getId(),
                                    document.getString("brand"),
                                    document.getString("model"),
                                    photoone,
                                    mileage,
                                    document.getLong("price")
                            );

                            carList.add(car);
                        }
                        adminCarAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("AdminActivity", "Ошибка загрузки машин", task.getException());
                    }
                });
    }
}
