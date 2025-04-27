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

public class SelectModelActivity extends AppCompatActivity {

    private Spinner modelSpinner;
    private Button nextButton;
    private FirebaseFirestore db;
    private List<String> modelList;
    private String selectedBrand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_model);

        modelSpinner = findViewById(R.id.modelSpinner);
        nextButton = findViewById(R.id.btnNext);
        db = FirebaseFirestore.getInstance();
        modelList = new ArrayList<>();

        selectedBrand = getIntent().getStringExtra("selectedBrand");
        if (selectedBrand == null || selectedBrand.isEmpty()) {
            Toast.makeText(this, "Ошибка: марка не выбрана", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadModels();

        nextButton.setOnClickListener(v -> {
            String selectedModel = modelSpinner.getSelectedItem().toString();
            if (selectedModel.equals("Выберите модель")) {
                Toast.makeText(this, "Пожалуйста, выберите модель", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(SelectModelActivity.this, CreateListingActivity.class);
                intent.putExtra("selectedBrand", selectedBrand);
                intent.putExtra("selectedModel", selectedModel);
                startActivity(intent);
            }
        });
    }

    private void loadModels() {
        db.collection("dfd").document("models")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            if (data != null && data.containsKey(selectedBrand)) {
                                List<String> models = (List<String>) data.get(selectedBrand);
                                if (models != null && !models.isEmpty()) {
                                    modelList.add("Выберите модель");
                                    modelList.addAll(models);
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                            android.R.layout.simple_spinner_item, modelList);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    modelSpinner.setAdapter(adapter);
                                } else {
                                    Toast.makeText(this, "Модели для " + selectedBrand + " не найдены", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e("SelectModelActivity", "Марка " + selectedBrand + " не найдена");
                                Toast.makeText(this, "Марка не найдена", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("SelectModelActivity", "Документ models не найден");
                            Toast.makeText(this, "Модели не найдены", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("SelectModelActivity", "Ошибка загрузки: ", task.getException());
                        Toast.makeText(this, "Ошибка загрузки моделей", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}