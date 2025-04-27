package com.example.drivee;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;
import java.util.*;

public class ManageModelsActivity extends AppCompatActivity {

    EditText brandInput, modelInput;
    Button addBrandBtn, addModelBtn;
    Spinner brandSpinner;
    FirebaseFirestore db;
    ArrayAdapter<String> brandAdapter;
    List<String> brandList = new ArrayList<>();
    Map<String, List<String>> allData = new HashMap<>();
    DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_models);

        brandInput = findViewById(R.id.brandInput);
        modelInput = findViewById(R.id.modelInput);
        addBrandBtn = findViewById(R.id.addBrandBtn);
        addModelBtn = findViewById(R.id.addModelBtn);
        brandSpinner = findViewById(R.id.brandSpinner);

        db = FirebaseFirestore.getInstance();
        docRef = db.collection("dfd").document("models");

        brandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, brandList);
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brandSpinner.setAdapter(brandAdapter);

        loadBrands();

        addBrandBtn.setOnClickListener(v -> {
            String brand = brandInput.getText().toString().trim();
            if (!brand.isEmpty()) {
                docRef.update(brand, new ArrayList<String>())
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Марка добавлена", Toast.LENGTH_SHORT).show();
                            loadBrands();
                        })
                        .addOnFailureListener(e -> {
                            Map<String, Object> newBrand = new HashMap<>();
                            newBrand.put(brand, new ArrayList<String>());
                            docRef.set(newBrand, SetOptions.merge());
                            Toast.makeText(this, "Марка создана", Toast.LENGTH_SHORT).show();
                            loadBrands();
                        });
            }
        });

        addModelBtn.setOnClickListener(v -> {
            String model = modelInput.getText().toString().trim();
            String selectedBrand = brandSpinner.getSelectedItem().toString();

            if (!model.isEmpty() && selectedBrand != null) {
                docRef.get().addOnSuccessListener(snapshot -> {
                    List<String> models = (List<String>) snapshot.get(selectedBrand);
                    if (models == null) models = new ArrayList<>();
                    if (!models.contains(model)) {
                        models.add(model);
                        docRef.update(selectedBrand, models);
                        Toast.makeText(this, "Модель добавлена", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Модель уже существует", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadBrands() {
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                brandList.clear();
                allData.clear();
                Map<String, Object> data = documentSnapshot.getData();
                for (String brand : data.keySet()) {
                    brandList.add(brand);
                    allData.put(brand, (List<String>) data.get(brand));
                }
                brandAdapter.notifyDataSetChanged();
            }
        });
    }
}
