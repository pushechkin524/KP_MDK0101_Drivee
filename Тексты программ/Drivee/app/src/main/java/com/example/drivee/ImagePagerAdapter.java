package com.example.drivee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private final Context context;
    private List<String> photoBase64List;

    public ImagePagerAdapter(Context context, List<String> photoBase64List) {
        this.context = context;
        this.photoBase64List = photoBase64List;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String base64Image = photoBase64List.get(position);

        if (!base64Image.isEmpty()) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);
        } else {
            holder.imageView.setImageResource(R.drawable.logo);
        }
    }

    @Override
    public int getItemCount() {
        return photoBase64List.size();
    }

    public void updateImages(List<String> newPhotoBase64List) {
        this.photoBase64List = newPhotoBase64List;
        notifyDataSetChanged();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
