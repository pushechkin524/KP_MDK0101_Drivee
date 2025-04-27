package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button resetPasswordButton = findViewById(R.id.btnResetPassword);
        resetPasswordButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
        });

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.passwordtxt);
        loginButton = findViewById(R.id.btnVhod);
        registerButton = findViewById(R.id.btnReg);

        loginButton.setOnClickListener(v -> loginUser());

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        String role = document.getString("role");

                        if ("admin".equals(role)) {
                            startActivity(new Intent(MainActivity.this, AdminActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, AllActivity.class));
                        }

                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка загрузки роли", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(user.getUid()).get()
                                    .addOnCompleteListener(roleTask -> {
                                        if (roleTask.isSuccessful() && roleTask.getResult().exists()) {
                                            DocumentSnapshot document = roleTask.getResult();
                                            String role = document.getString("role");

                                            if ("admin".equals(role)) {
                                                startActivity(new Intent(MainActivity.this, AdminActivity.class));
                                            } else {
                                                startActivity(new Intent(MainActivity.this, AllActivity.class));
                                            }

                                            finish();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Ошибка загрузки роли", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Ошибка авторизации: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
