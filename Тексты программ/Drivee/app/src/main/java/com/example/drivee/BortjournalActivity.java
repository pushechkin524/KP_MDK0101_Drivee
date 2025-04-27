package com.example.drivee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BortjournalActivity extends AppCompatActivity {

    private RecyclerView postsRecyclerView;
    private Button createPostButton;
    private FirebaseFirestore db;
    private List<BortjournalPost> posts = new ArrayList<>();
    private BortjournalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bortjournal_activity);

        postsRecyclerView = findViewById(R.id.postsRecyclerView);
        createPostButton = findViewById(R.id.createPostButton);

        db = FirebaseFirestore.getInstance();

        adapter = new BortjournalAdapter(this, posts);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(adapter);

        createPostButton.setOnClickListener(v -> {
            startActivity(new Intent(this, CreatePostActivity.class));
        });

        loadPosts();
    }

    private void loadPosts() {
        db.collection("bortjornal")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    posts.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        BortjournalPost post = doc.toObject(BortjournalPost.class);
                        posts.add(post);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

}
