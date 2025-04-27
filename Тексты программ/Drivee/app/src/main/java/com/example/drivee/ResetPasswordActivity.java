package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton, backToLoginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.btnResetPassword);
        backToLoginButton = findViewById(R.id.btnBackToLogin);

        mAuth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(ResetPasswordActivity.this, "Введите ваш Email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this, "Инструкция по сбросу пароля отправлена на почту", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Ошибка: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
