package com.example.tp8redo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteAudioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        List<Uri> favoriteAudioFiles = getIntent().getParcelableArrayListExtra("favoriteAudioFiles");

        recyclerView = findViewById(R.id.favoriteAudioRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FavoriteAudioAdapter(this, favoriteAudioFiles); // Pass the context as the first argument
        recyclerView.setAdapter(adapter);
    }
}