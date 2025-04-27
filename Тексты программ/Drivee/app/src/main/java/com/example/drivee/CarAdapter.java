package com.example.drivee;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DecimalFormat;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private Context context;
    private List<Car> carList;

    private boolean showStatus = true;
    private boolean showFavorite = true;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CarAdapter(Context context, List<Car> carList) {
        this(context, carList, true, false);
    }

    public CarAdapter(Context context, List<Car> carList, boolean showFavorite, boolean showStatus) {
        this.context = context;
        this.carList = carList;
        this.showFavorite = showFavorite;
        this.showStatus = showStatus;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);

        holder.carTitleTextView.setText(car.getBrand() + " " + car.getModel());
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(car.getPrice()).replace(",", " ");
        holder.carPriceTextView.setText(formattedPrice + " ₽");

        // 🟡 Обработка статуса (с переводом)
        if (showStatus && holder.carStatusTextView != null && car.getStatus() != null) {
            holder.carStatusTextView.setVisibility(View.VISIBLE);

            String statusTranslated;
            switch (car.getStatus()) {
                case "approved":
                    statusTranslated = "Подтверждено";
                    break;
                case "pending":
                    statusTranslated = "На проверке";
                    break;
                case "rejected":
                    statusTranslated = "Отклонено";
                    break;
                default:
                    statusTranslated = car.getStatus();
                    break;
            }

            holder.carStatusTextView.setText("Статус: " + statusTranslated);
        } else if (holder.carStatusTextView != null) {
            holder.carStatusTextView.setVisibility(View.GONE);
        }

        // Фото из base64
        String base64 = car.getPhotoBase64();
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.carImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                holder.carImageView.setImageResource(R.drawable.logo);
            }
        } else {
            holder.carImageView.setImageResource(R.drawable.logo);
        }

        // Кнопка "избранное"
        if (showFavorite) {
            holder.favoriteButton.setVisibility(View.VISIBLE);
            updateFavoriteIcon(holder.favoriteButton, car.isFavorite());

            holder.favoriteButton.setOnClickListener(v -> {
                boolean isNowFavorite = !car.isFavorite();
                car.setFavorite(isNowFavorite);
                updateFavoriteIcon(holder.favoriteButton, isNowFavorite);
                updateFavoriteInFirebase(car.getId(), isNowFavorite);
            });
        } else {
            holder.favoriteButton.setVisibility(View.GONE); // скрываем в профиле
        }
        if (showStatus && holder.deleteButton != null) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                String carId = car.getId();
                db.collection("cars").document(carId)
                        .delete()
                        .addOnSuccessListener(unused -> {
                            carList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Машина удалена", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show());
            });
        } else if (holder.deleteButton != null) {
            holder.deleteButton.setVisibility(View.GONE);
        }
        long mileage = 0;
        try {
            java.lang.reflect.Field mileageField = car.getClass().getDeclaredField("mileage");
            mileageField.setAccessible(true);
            Object value = mileageField.get(car);
            if (value instanceof Long) mileage = (Long) value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.carMileageTextView.setText("Пробег: " + mileage + " км");


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CenterActivity.class);
            intent.putExtra("carId", car.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView carImageView;
        TextView carTitleTextView, carPriceTextView, carStatusTextView, deleteButton;
        TextView carMileageTextView;

        ImageButton favoriteButton;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            carImageView = itemView.findViewById(R.id.carImageView);
            carTitleTextView = itemView.findViewById(R.id.carTitleTextView);
            carPriceTextView = itemView.findViewById(R.id.carPriceTextView);
            carMileageTextView = itemView.findViewById(R.id.carMileageTextView);
            carStatusTextView = itemView.findViewById(R.id.carStatusTextView);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);
            deleteButton = itemView.findViewById(R.id.deleteCarButton); // Добавили
        }
    }


    private void updateFavoriteIcon(ImageButton button, boolean isFavorite) {
        if (isFavorite) {
            button.setImageResource(R.drawable.ic_heart_filled);
        } else {
            button.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    private void updateFavoriteInFirebase(String carId, boolean add) {
        String userId = auth.getCurrentUser().getUid();
        if (add) {
            db.collection("users").document(userId)
                    .update("favorites", FieldValue.arrayUnion(carId));
        } else {
            db.collection("users").document(userId)
                    .update("favorites", FieldValue.arrayRemove(carId));
        }
    }
}
