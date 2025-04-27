package com.example.drivee;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AdminCarAdapter extends RecyclerView.Adapter<AdminCarAdapter.CarViewHolder> {

    private Context context;
    private List<Car> carList;

    public AdminCarAdapter(Context context, List<Car> carList) {
        this.context = context;
        this.carList = carList;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_car_admin, parent, false);
        return new CarViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);

        holder.tvBrand.setText(car.getBrand());
        holder.tvModel.setText(car.getModel());
        holder.tvPrice.setText(String.format("%d руб.", car.getPrice()));

        holder.btnAccept.setOnClickListener(v -> {
            holder.btnAccept.setEnabled(false); // анти-спам
            updateCarStatus(car.getId(), "approved", position);
        });

        holder.btnDelete.setOnClickListener(v -> {
            holder.btnDelete.setEnabled(false);
            deleteCar(car.getId(), position);
        });

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

    public class CarViewHolder extends RecyclerView.ViewHolder {

        TextView tvBrand, tvModel, tvPrice;
        Button btnAccept, btnDelete;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvModel = itemView.findViewById(R.id.tvModel);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void updateCarStatus(String carId, String status, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cars").document(carId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Объявление утверждено!", Toast.LENGTH_SHORT).show();
                    carList.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteCar(String carId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cars").document(carId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Объявление удалено!", Toast.LENGTH_SHORT).show();
                    carList.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
