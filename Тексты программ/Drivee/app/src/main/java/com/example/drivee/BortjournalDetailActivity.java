package com.example.drivee;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BortjournalDetailActivity extends AppCompatActivity {

    private TextView detailBrandModel, detailTitle, detailContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bortjournal_detail_activity);

        detailBrandModel = findViewById(R.id.detailBrandModel);
        detailTitle = findViewById(R.id.detailTitle);
        detailContent = findViewById(R.id.detailContent);

        String brand = getIntent().getStringExtra("brand");
        String model = getIntent().getStringExtra("model");
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");

        detailBrandModel.setText(brand + " " + model);
        detailTitle.setText(title);
        detailContent.setText(content);
    }
}
