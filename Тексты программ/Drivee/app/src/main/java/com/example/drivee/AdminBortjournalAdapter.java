package com.example.drivee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminBortjournalAdapter extends RecyclerView.Adapter<AdminBortjournalAdapter.ViewHolder> {

    public interface OnPostOpenListener {
        void onOpen(BortjournalPost post);
    }

    public interface OnPostApproveListener {
        void onApprove(BortjournalPost post);
    }

    public interface OnPostDeleteListener {
        void onDelete(BortjournalPost post);
    }

    private List<BortjournalPost> posts;
    private OnPostOpenListener openListener;
    private OnPostApproveListener approveListener;
    private OnPostDeleteListener deleteListener;

    public AdminBortjournalAdapter(List<BortjournalPost> posts,
                                   OnPostOpenListener openListener,
                                   OnPostApproveListener approveListener,
                                   OnPostDeleteListener deleteListener) {
        this.posts = posts;
        this.openListener = openListener;
        this.approveListener = approveListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AdminBortjournalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_bortjournal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBortjournalAdapter.ViewHolder holder, int position) {
        BortjournalPost post = posts.get(position);

        holder.brandModelTextView.setText(post.getBrand() + " " + post.getModel());
        holder.titleTextView.setText(post.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (openListener != null) openListener.onOpen(post);
        });

        holder.approveButton.setOnClickListener(v -> {
            if (approveListener != null) approveListener.onApprove(post);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(post);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, brandModelTextView;
        Button approveButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            brandModelTextView = itemView.findViewById(R.id.adminBrandModel);
            titleTextView = itemView.findViewById(R.id.adminTitle);
            approveButton = itemView.findViewById(R.id.approveButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
