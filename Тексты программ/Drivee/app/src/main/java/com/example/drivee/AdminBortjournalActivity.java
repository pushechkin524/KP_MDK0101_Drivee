package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminBortjournalActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<BortjournalPost> postList = new ArrayList<>();
    private AdminBortjournalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bortjournal);

        recyclerView = findViewById(R.id.adminRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        adapter = new AdminBortjournalAdapter(postList, this::openDetail, this::approvePost, this::deletePost);
        recyclerView.setAdapter(adapter);

        loadPendingPosts();
    }

    private void loadPendingPosts() {
        db.collection("bortjornal")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    postList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        BortjournalPost post = doc.toObject(BortjournalPost.class);
                        post.setId(doc.getId());
                        postList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show());
    }

    private void openDetail(BortjournalPost post) {
        Intent intent = new Intent(this, BortjournalDetailActivity.class);
        intent.putExtra("brand", post.getBrand());
        intent.putExtra("model", post.getModel());
        intent.putExtra("title", post.getTitle());
        intent.putExtra("content", post.getContent());
        startActivity(intent);
    }

    private void approvePost(BortjournalPost post) {
        db.collection("bortjornal").document(post.getId())
                .update("status", "approved")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Подтверждено", Toast.LENGTH_SHORT).show();
                    postList.remove(post);
                    adapter.notifyDataSetChanged();
                });
    }

    private void deletePost(BortjournalPost post) {
        db.collection("bortjornal").document(post.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Удалено", Toast.LENGTH_SHORT).show();
                    postList.remove(post);
                    adapter.notifyDataSetChanged();
                });
    }
}
