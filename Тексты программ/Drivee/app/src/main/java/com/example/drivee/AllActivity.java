package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarAdapter carAdapter;
    private Button resetFiltersButton, paramsButton;

    private List<Car> carList;
    private Spinner brandSpinner, modelSpinner;
    private List<String> brandList = new ArrayList<>();
    private List<String> modelList = new ArrayList<>();
    private String selectedBrand = null;
    private String selectedModel = null;

    private ImageButton filterButton;

    private FirebaseFirestore db;
    private Set<String> favoriteCarIds = new HashSet<>();
    private FirebaseAuth auth;
    private TextView adsCountTextView;

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.nav_home) {
                        startActivity(new Intent(AllActivity.this, AllActivity.class));
                        return true;
                    } else if (id == R.id.nav_saved) {
                        startActivity(new Intent(AllActivity.this, FavoritesActivity.class));
                        return true;
                    } else if (id == R.id.nav_create) {
                        startActivity(new Intent(AllActivity.this, SelectBrandActivity.class));
                        return true;
                    } else if (id == R.id.nav_profile) {
                        startActivity(new Intent(AllActivity.this, UserProfileActivity.class));
                        return true;
                    } else if (id == R.id.nav_garage) {
                        startActivity(new Intent(AllActivity.this, BortjournalActivity.class));
                        return true;
                    }
                    return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        carList = new ArrayList<>();
        carAdapter = new CarAdapter(this, carList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(carAdapter);

        adsCountTextView = findViewById(R.id.adsCountTextView);
        filterButton = findViewById(R.id.filterButton);
        brandSpinner = findViewById(R.id.brandSpinner);
        modelSpinner = findViewById(R.id.modelSpinner);
        resetFiltersButton = findViewById(R.id.resetFiltersButton);
        paramsButton = findViewById(R.id.paramsButton);
        paramsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FilterActivity.class);
            startActivityForResult(intent, 101);
        });


        filterButton.setOnClickListener(v -> showFilterMenu());

        resetFiltersButton.setOnClickListener(v -> {
            brandSpinner.setSelection(0);
            modelSpinner.setSelection(0);
            selectedBrand = null;
            selectedModel = null;
            loadCars();
        });



        loadFavoriteCars();
        loadCars();
        loadBrands();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }private void applyAdvancedFilters(Intent data) {
        db.collection("cars")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        boolean isValid = true;

                        Long year = doc.getLong("year");
                        Double volume = doc.getDouble("volume");
                        Long horsepower = doc.getLong("horsepower");
                        Long owners = doc.getLong("owners");
                        Long price = doc.getLong("price");

                        String color = doc.getString("color");
                        String body = doc.getString("body");
                        String drive = doc.getString("drive");
                        String fuel = doc.getString("fuel");
                        String transmission = doc.getString("transmission");
                        String city = doc.getString("city");

                        isValid &= checkRange(year, data.getStringExtra("yearFrom"), data.getStringExtra("yearTo"));
                        isValid &= checkRange(volume, data.getStringExtra("volumeFrom"), data.getStringExtra("volumeTo"));
                        isValid &= checkRange(horsepower, data.getStringExtra("powerFrom"), data.getStringExtra("powerTo"));
                        isValid &= checkRange(owners, null, data.getStringExtra("ownersTo"));
                        isValid &= checkRange(price, data.getStringExtra("priceFrom"), data.getStringExtra("priceTo"));

                        isValid &= checkString(color, data.getStringExtra("color"));
                        isValid &= checkString(body, data.getStringExtra("body"));
                        isValid &= checkString(drive, data.getStringExtra("drive"));
                        isValid &= checkString(fuel, data.getStringExtra("fuel"));
                        isValid &= checkString(transmission, data.getStringExtra("transmission"));
                        isValid &= checkString(city, data.getStringExtra("city"));

                        if (isValid) {
                            String photoone = doc.getString("photoone");
                            if (photoone == null) photoone = "";

                            long mileage = doc.getLong("mileage") != null ? doc.getLong("mileage") : 0;

                            Car car = new Car(
                                    doc.getId(),
                                    doc.getString("brand"),
                                    doc.getString("model"),
                                    photoone,
                                    mileage,
                                    doc.getLong("price")
                            );

                            car.setFavorite(favoriteCarIds.contains(car.getId()));
                            carList.add(car);
                        }
                    }

                    adsCountTextView.setText("Найдено объявлений: " + carList.size());
                    carAdapter.notifyDataSetChanged();
                });
    }

    private boolean checkRange(Number value, String fromStr, String toStr) {
        if (value == null) return false;

        try {
            double doubleValue = value.doubleValue();

            if (fromStr != null && doubleValue < Double.parseDouble(fromStr)) return false;
            if (toStr != null && doubleValue > Double.parseDouble(toStr)) return false;
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean checkString(String fieldValue, String expected) {
        if (expected == null) return true; // Не фильтруем
        return expected.equalsIgnoreCase(fieldValue);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            applyAdvancedFilters(data);
        }
    }


    private void loadFavoriteCars() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("favorites")) {
                        List<String> favoriteCarIdsList = (List<String>) documentSnapshot.get("favorites");
                        if (favoriteCarIdsList != null) {
                            favoriteCarIds.addAll(favoriteCarIdsList);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("AllActivity", "Ошибка загрузки избранных машин", e));
    }

    private void showFilterMenu() {
        android.widget.PopupMenu popupMenu = new android.widget.PopupMenu(this, filterButton);
        popupMenu.getMenu().add("Цена ↑");
        popupMenu.getMenu().add("Цена ↓");
        popupMenu.getMenu().add("Новые");

        popupMenu.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();

            switch (selected) {
                case "Цена ↑":
                    carList.sort((a, b) -> Long.compare(a.getPrice(), b.getPrice()));
                    break;
                case "Цена ↓":
                    carList.sort((a, b) -> Long.compare(b.getPrice(), a.getPrice()));
                    break;
                case "Новые":
                    carList.sort((a, b) -> b.getId().compareTo(a.getId()));
                    break;
            }

            carAdapter.notifyDataSetChanged();
            return true;
        });

        popupMenu.show();
    }
    private void loadBrands() {
        db.collection("dfd").document("models")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            brandList.clear();
                            brandList.add("Марка");

                            brandList.addAll(data.keySet());

                            ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, brandList);
                            brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            brandSpinner.setAdapter(brandAdapter);

                            brandSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                                    if (position == 0) return;
                                    selectedBrand = brandList.get(position);
                                    loadModels(selectedBrand, data);
                                }

                                @Override
                                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                            });
                        }
                    }
                });
    }
    private void loadModels(String brand, Map<String, Object> data) {
        Object rawModels = data.get(brand);
        if (rawModels instanceof List) {
            List<String> models = (List<String>) rawModels;
            modelList.clear();
            modelList.add("Модель");
            modelList.addAll(models);

            ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, modelList);
            modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modelSpinner.setAdapter(modelAdapter);

            modelSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) return;
                    selectedModel = modelList.get(position);
                    filterCars();
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }
    }
    private void filterCars() {
        db.collection("cars")
                .whereEqualTo("status", "approved")
                .whereEqualTo("brand", selectedBrand)
                .whereEqualTo("model", selectedModel)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    carList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
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

                        car.setFavorite(favoriteCarIds.contains(car.getId()));
                        carList.add(car);
                    }

                    adsCountTextView.setText("Найдено объявлений: " + carList.size());
                    carAdapter.notifyDataSetChanged();
                });
    }


    private long getMileage(Car car) {
        try {
            java.lang.reflect.Field mileageField = car.getClass().getDeclaredField("mileage");
            mileageField.setAccessible(true);
            Object value = mileageField.get(car);
            return value instanceof Long ? (Long) value : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private void loadCars() {
        db.collection("cars")
                .whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        carList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String photoone = document.getString("photoone");
                            if (photoone == null) {
                                photoone = "";
                            }

                            long mileage = document.getLong("mileage") != null ? document.getLong("mileage") : 0;

                            Car car = new Car(
                                    document.getId(),
                                    document.getString("brand"),
                                    document.getString("model"),
                                    photoone,
                                    mileage,
                                    document.getLong("price")
                            );
                            car.setFavorite(favoriteCarIds.contains(car.getId()));
                            carList.add(car);
                        }

                        adsCountTextView.setText(carList.size() + " предложений");

                        carAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("AllActivity", "Ошибка загрузки машин", task.getException());
                    }
                });
    }
}
