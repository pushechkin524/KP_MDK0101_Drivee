package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText, phoneEditText, dobEditText;
    private Button registerButton, backToLoginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.passwordtxt);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        dobEditText = findViewById(R.id.dobEditText);
        registerButton = findViewById(R.id.btnRegister);
        backToLoginButton = findViewById(R.id.btnBackToLogin);

        formatPhoneInput();

        registerButton.setOnClickListener(v -> registerUser());

        backToLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(RegActivity.this, MainActivity.class));
            finish();
        });
    }

    private void formatPhoneInput() {
        phoneEditText.addTextChangedListener(new TextWatcher() {
            boolean isFormatting;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String digits = s.toString().replaceAll("\\D", "");
                if (digits.length() > 11) digits = digits.substring(0, 11);

                StringBuilder formatted = new StringBuilder();
                int len = digits.length();

                if (len > 0) formatted.append("+7");
                if (len > 1) formatted.append(" (").append(digits.substring(1, Math.min(4, len)));
                if (len > 4) formatted.append(") ").append(digits.substring(4, Math.min(7, len)));
                if (len > 7) formatted.append("-").append(digits.substring(7, Math.min(9, len)));
                if (len > 9) formatted.append("-").append(digits.substring(9));

                phoneEditText.setText(formatted.toString());
                phoneEditText.setSelection(phoneEditText.getText().length());

                isFormatting = false;
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            nameEditText.setError("Введите имя");
            isValid = false;
        }

        if (dob.isEmpty()) {
            dobEditText.setError("Введите дату рождения");
            isValid = false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Некорректный email");
            isValid = false;
        }

        if (password.length() < 8 || !password.matches(".*\\d.*") || !password.matches(".*[A-Z].*")) {
            passwordEditText.setError("Минимум 8 символов, 1 цифра, 1 заглавная");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Пароли не совпадают");
            isValid = false;
        }

        if (!phone.matches("^\\+7 \\(\\d{3}\\) \\d{3}-\\d{2}-\\d{2}$")) {
            phoneEditText.setError("Формат: +7 (999) 123-45-67");
            isValid = false;
        }

        if (!isValid) return;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserData(user.getUid(), name, email, phone, dob);

                        Toast.makeText(RegActivity.this, "Добро пожаловать, " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegActivity.this, CenterActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegActivity.this, "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserData(String userId, String name, String email, String phone, String dob) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("role", "user");
        user.put("date_of_birth", dob);
        user.put("account_creation_date", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegActivity.this, "Пользователь зарегистрирован", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegActivity.this, "Ошибка сохранения данных", Toast.LENGTH_SHORT).show();
                });
    }
}
