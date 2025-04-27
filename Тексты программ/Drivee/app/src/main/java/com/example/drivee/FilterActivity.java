package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class FilterActivity extends AppCompatActivity {

    private EditText yearFrom, yearTo, volumeFrom, volumeTo, powerFrom, powerTo, ownersTo, priceFrom, priceTo, cityInput;
    private Spinner colorSpinner, bodySpinner, driveSpinner, fuelSpinner, transmissionSpinner;
    private Button applyButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        yearFrom = findViewById(R.id.yearFrom);
        yearTo = findViewById(R.id.yearTo);
        volumeFrom = findViewById(R.id.volumeFrom);
        volumeTo = findViewById(R.id.volumeTo);
        powerFrom = findViewById(R.id.powerFrom);
        powerTo = findViewById(R.id.powerTo);
        ownersTo = findViewById(R.id.ownersTo);
        priceFrom = findViewById(R.id.priceFrom);
        priceTo = findViewById(R.id.priceTo);
        cityInput = findViewById(R.id.cityInput);

        colorSpinner = findViewById(R.id.colorSpinner);
        bodySpinner = findViewById(R.id.bodySpinner);
        driveSpinner = findViewById(R.id.driveSpinner);
        fuelSpinner = findViewById(R.id.fuelSpinner);
        transmissionSpinner = findViewById(R.id.transmissionSpinner);

        applyButton = findViewById(R.id.applyButton);
        cancelButton = findViewById(R.id.cancelButton);

        setupSpinners();

        applyButton.setOnClickListener(v -> returnFilters());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        String[] fuels = {"Топливо", "Бензин", "Дизель", "Гибрид", "Электро", "ГБО", "Метан"};
        String[] bodies = {"Тип кузова", "Купе", "Седан", "Хэтчбек", "Кроссовер", "Внедорожник"};
        String[] drives = {"Привод", "Полный", "Передний", "Задний"};
        String[] transmissions = {"Коробка", "Автоматическая", "Механика", "Вариатор", "Робот"};
        String[] colors = {"Цвет", "Красный", "Черный", "Белый", "Синий", "Зеленый", "Желтый", "Серый"};

        setupSpinner(colorSpinner, colors);
        setupSpinner(bodySpinner, bodies);
        setupSpinner(driveSpinner, drives);
        setupSpinner(fuelSpinner, fuels);
        setupSpinner(transmissionSpinner, transmissions);
    }

    private void setupSpinner(Spinner spinner, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void returnFilters() {
        Intent result = new Intent();

        result.putExtra("yearFrom", getTextOrNull(yearFrom));
        result.putExtra("yearTo", getTextOrNull(yearTo));
        result.putExtra("volumeFrom", getTextOrNull(volumeFrom));
        result.putExtra("volumeTo", getTextOrNull(volumeTo));
        result.putExtra("powerFrom", getTextOrNull(powerFrom));
        result.putExtra("powerTo", getTextOrNull(powerTo));
        result.putExtra("ownersTo", getTextOrNull(ownersTo));
        result.putExtra("priceFrom", getTextOrNull(priceFrom));
        result.putExtra("priceTo", getTextOrNull(priceTo));
        result.putExtra("city", getTextOrNull(cityInput));

        result.putExtra("color", getSpinnerValue(colorSpinner));
        result.putExtra("body", getSpinnerValue(bodySpinner));
        result.putExtra("drive", getSpinnerValue(driveSpinner));
        result.putExtra("fuel", getSpinnerValue(fuelSpinner));
        result.putExtra("transmission", getSpinnerValue(transmissionSpinner));

        setResult(RESULT_OK, result);
        finish();
    }

    private String getTextOrNull(EditText editText) {
        String text = editText.getText().toString().trim();
        return TextUtils.isEmpty(text) ? null : text;
    }

    private String getSpinnerValue(Spinner spinner) {
        if (spinner.getSelectedItemPosition() == 0) {
            return null;
        }
        return spinner.getSelectedItem().toString();
    }
}
