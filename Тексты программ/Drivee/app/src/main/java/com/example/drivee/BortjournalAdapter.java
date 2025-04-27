package com.example.drivee;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BortjournalAdapter extends RecyclerView.Adapter<BortjournalAdapter.PostViewHolder> {

    private Context context;
    private List<BortjournalPost> postList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean enableDeletion;

    public BortjournalAdapter(Context context, List<BortjournalPost> postList, boolean enableDeletion) {
        this.context = context;
        this.postList = postList;
        this.enableDeletion = enableDeletion;
    }

    // Дополнительный конструктор без флага (по умолчанию без удаления)
    public BortjournalAdapter(Context context, List<BortjournalPost> postList) {
        this(context, postList, false);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bortjournal, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        BortjournalPost post = postList.get(position);

        holder.brandModelTextView.setText(post.getBrand() + " " + post.getModel());
        holder.titleTextView.setText(post.getTitle());
        holder.mileageTextView.setText("Пробег: " + post.getMileage() + " км");

        // Загрузка имени и аватара
        db.collection("users").document(post.getUserId())
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    String imageBase64 = doc.getString("profile_image");

                    if (name != null) {
                        holder.userNameTextView.setText(name);
                    }

                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        try {
                            byte[] decoded = Base64.decode(imageBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            holder.profileImageView.setImageBitmap(getCircularBitmap(bitmap));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        holder.profileImageView.setImageResource(R.drawable.logo);
                    }
                });

        // Открытие детали по нажатию
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BortjournalDetailActivity.class);
            intent.putExtra("brand", post.getBrand());
            intent.putExtra("model", post.getModel());
            intent.putExtra("title", post.getTitle());
            intent.putExtra("content", post.getContent());
            context.startActivity(intent);
        });

        // Удаление по долгому нажатию — только если enableDeletion = true
        if (enableDeletion) {
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Удаление записи")
                        .setMessage("Вы действительно хотите удалить эту запись?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            String postId = post.getId();
                            db.collection("bortjornal").document(postId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        postList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, postList.size());
                                        Toast.makeText(context, "Запись удалена", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView brandModelTextView, titleTextView, mileageTextView, userNameTextView;
        ImageView profileImageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            brandModelTextView = itemView.findViewById(R.id.brandModelTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            mileageTextView = itemView.findViewById(R.id.mileageTextView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);

        return output;
    }
}
