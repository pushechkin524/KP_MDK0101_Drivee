package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectBrandActivity extends AppCompatActivity {

    private Spinner brandSpinner;
    private Button nextButton;
    private FirebaseFirestore db;
    private List<String> brandList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_brand);

        brandSpinner = findViewById(R.id.brandSpinner);
        nextButton = findViewById(R.id.btnNext);
        db = FirebaseFirestore.getInstance();
        brandList = new ArrayList<>();

        loadBrands();

        nextButton.setOnClickListener(v -> {
            String selectedBrand = brandSpinner.getSelectedItem().toString();
            if (selectedBrand.equals("Выберите марку")) {
                Toast.makeText(this, "Пожалуйста, выберите марку", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(SelectBrandActivity.this, SelectModelActivity.class);
                intent.putExtra("selectedBrand", selectedBrand);
                startActivity(intent);
            }
        });
    }

    private void loadBrands() {
        db.collection("dfd").document("models")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                brandList.add("Выберите марку");
                                for (Object value : data.keySet()) { // Используем keySet(), так как марки — это ключи
                                    brandList.add(value.toString());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                        android.R.layout.simple_spinner_item, brandList);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                brandSpinner.setAdapter(adapter);
                            }
                        } else {
                            Log.e("SelectBrandActivity", "Документ models не найден");
                            Toast.makeText(this, "Марки не найдены", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("SelectBrandActivity", "Ошибка загрузки: ", task.getException());
                        Toast.makeText(this, "Ошибка загрузки марок", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}