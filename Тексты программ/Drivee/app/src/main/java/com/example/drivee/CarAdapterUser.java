package com.example.drivee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class CarAdapterUser extends RecyclerView.Adapter<CarAdapterUser.CarViewHolder> {

    private Context context;
    private List<UserCar> carList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CarAdapterUser(Context context, List<UserCar> carList) {
        this.context = context;
        this.carList = carList;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car_user, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        UserCar car = carList.get(position);
        holder.title.setText(car.getBrand() + " " + car.getModel());
        holder.price.setText(car.getPrice() + " ₽");
        holder.status.setText("Статус: " + car.getStatus());

        // Загрузка фото
        if (car.getPhotoUrl() != null && !car.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(car.getPhotoUrl())
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.logo);
        }

        holder.deleteButton.setOnClickListener(v -> {
            db.collection("cars").document(car.getId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        carList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, carList.size());
                        Toast.makeText(context, "Объявление удалено", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, price, status, deleteButton;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carImageView);
            title = itemView.findViewById(R.id.carTitleTextView);
            price = itemView.findViewById(R.id.carPriceTextView);
            status = itemView.findViewById(R.id.carStatusTextView);
            deleteButton = itemView.findViewById(R.id.deleteCarButton);
        }
    }
}
