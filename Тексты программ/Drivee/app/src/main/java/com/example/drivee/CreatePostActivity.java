package com.example.drivee;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private Spinner brandSpinner, modelSpinner;
    private EditText titleEditText, contentEditText, mileageEditText;
    private Button publishButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Map<String, List<String>> brandModelMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post_activity);

        brandSpinner = findViewById(R.id.brandSpinner);
        modelSpinner = findViewById(R.id.modelSpinner);
        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        mileageEditText = findViewById(R.id.mileageEditText);
        publishButton = findViewById(R.id.publishButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadBrands();

        brandSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int pos, long id) {
                String selectedBrand = brandSpinner.getSelectedItem().toString();
                loadModelsForBrand(selectedBrand);
            }

            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        publishButton.setOnClickListener(v -> publishPost());
    }

    private void loadBrands() {
        db.collection("dfd").document("models").get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> data = documentSnapshot.getData();
                if (data != null) {
                    List<String> brands = new ArrayList<>();
                    for (String key : data.keySet()) {
                        brands.add(key);
                        brandModelMap.put(key, (List<String>) data.get(key));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, brands);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    brandSpinner.setAdapter(adapter);
                }
            }
        });
    }

    private void loadModelsForBrand(String brand) {
        List<String> models = brandModelMap.get(brand);
        if (models != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, models);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modelSpinner.setAdapter(adapter);
        }
    }

    private void publishPost() {
        String brand = brandSpinner.getSelectedItem().toString();
        String model = modelSpinner.getSelectedItem().toString();
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();
        String mileageStr = mileageEditText.getText().toString().trim();

        if (TextUtils.isEmpty(brand) || TextUtils.isEmpty(model) || TextUtils.isEmpty(title)
                || TextUtils.isEmpty(content) || TextUtils.isEmpty(mileageStr)) {
            Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        long mileage;
        try {
            mileage = Long.parseLong(mileageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Некорректный пробег", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("brand", brand);
        postMap.put("model", model);
        postMap.put("title", title);
        postMap.put("content", content);
        postMap.put("mileage", mileage);
        postMap.put("userId", userId);
        postMap.put("status", "pending");
        postMap.put("timestamp", FieldValue.serverTimestamp());

        db.collection("bortjornal")
                .add(postMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Запись отправлена на модерацию", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                });
    }
}
