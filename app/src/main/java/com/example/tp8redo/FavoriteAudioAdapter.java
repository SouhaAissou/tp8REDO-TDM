package com.example.tp8redo;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;


public class FavoriteAudioAdapter extends RecyclerView.Adapter<FavoriteAudioAdapter.ViewHolder> {

    private List<Uri> favoriteAudioFiles;
    private Context context;
    private MediaPlayer mediaPlayer;
    private Uri currentlyPlayingAudio;

    public FavoriteAudioAdapter(Context context, List<Uri> favoriteAudioFiles) {
        this.context = context;
        this.favoriteAudioFiles = favoriteAudioFiles;
        mediaPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_favorite_audio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri audioUri = favoriteAudioFiles.get(position);

        // Get the file name from the Uri
        String fileName = getFileNameFromUri(audioUri);

        // Set the audio file name in the TextView
        holder.audioNameTextView.setText(fileName);

        // Set click listener for the list item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(audioUri);
            }
        });

        // Set long click listener for the list item
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                showConfirmationDialog(clickedPosition);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteAudioFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView audioNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            audioNameTextView = itemView.findViewById(R.id.audioNameTextView);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = null;
        try {
            ContentResolver resolver = context.getContentResolver();
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            cursor = resolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                fileName = cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }

    private void playAudio(Uri audioUri) {
        if (currentlyPlayingAudio != null && currentlyPlayingAudio.equals(audioUri)) {
            // The same audio is already playing, do nothing
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(context.getApplicationContext(), audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentlyPlayingAudio = audioUri;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to delete this audio?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeAudio(position);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeAudio(int position) {
        Uri audioUri = favoriteAudioFiles.get(position);
        favoriteAudioFiles.remove(position);
        notifyItemRemoved(position);

        // Delete audio file from the database
        deleteAudioFromDatabase(audioUri);
    }

    private void deleteAudioFromDatabase(Uri audioUri) {
        // Create an instance of the DatabaseHelper
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // Remove the audio file URI from the database
        dbHelper.removeFavoriteAudio(audioUri.toString());
    }
}
