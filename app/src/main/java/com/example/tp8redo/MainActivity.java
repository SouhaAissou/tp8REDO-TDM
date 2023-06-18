package com.example.tp8redo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private MediaPlayer mediaPlayer;
    private List<Uri> audioFiles;
    private int currentIndex;
    private Button playButton;
    private Button previousButton;
    private Button nextButton;
    private TextView audioFileNameTextView;
    private boolean isFavorite = false;
    private List<Uri> favoriteAudioFiles;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = new MediaPlayer();
        audioFiles = new ArrayList<>();
        currentIndex = 0;

        databaseHelper = new DatabaseHelper(this);

        favoriteAudioFiles = new ArrayList<>();
        loadFavoriteAudioFromDatabase();

        playButton = findViewById(R.id.playButton);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        audioFileNameTextView = findViewById(R.id.audioFileNameTextView);

        ImageButton favoriteButton = findViewById(R.id.favoriteButton);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPreviousAudio();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextAudio();
            }
        });

        if (checkPermission()) {
            retrieveAudioFiles();
        } else {
            requestPermission();
        }
    }

    private void toggleFavorite() {
        Uri currentAudioUri = audioFiles.get(currentIndex);

        if (favoriteAudioFiles.contains(currentAudioUri)) {
            favoriteAudioFiles.remove(currentAudioUri);
            databaseHelper.removeFavoriteAudio(currentAudioUri.toString());
            Toast.makeText(this, "Removed from favorites.", Toast.LENGTH_SHORT).show();
            isFavorite = false;
        } else {
            favoriteAudioFiles.add(currentAudioUri);
            databaseHelper.addFavoriteAudio(currentAudioUri.toString());
            Toast.makeText(this, "Added to favorites.", Toast.LENGTH_SHORT).show();
            isFavorite = true;
        }

        updateFavoriteButton();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.favorites) {
            openFavoritesPage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openFavoritesPage() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        intent.putParcelableArrayListExtra("favoriteAudioFiles", new ArrayList<>(favoriteAudioFiles));
        startActivity(intent);
    }
    private void updateFavoriteButton() {
        ImageButton favoriteButton = findViewById(R.id.favoriteButton);
        Uri currentAudioUri = audioFiles.get(currentIndex);

        if (favoriteAudioFiles.contains(currentAudioUri)) {
            favoriteButton.setImageResource(R.drawable.filledheart);
//            Toast.makeText(this, "Added to favorites.", Toast.LENGTH_SHORT).show();
            isFavorite = true;
        } else {
            favoriteButton.setImageResource(R.drawable.heart);
            isFavorite = false;
        }
    }

    private void playAudio() {
        if (audioFiles.isEmpty()) {
            Toast.makeText(this, "No audio files found.", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri audioUri = audioFiles.get(currentIndex);

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(getApplicationContext(), audioUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateUI();
    }

    private void playPreviousAudio() {
        if (currentIndex > 0) {
            currentIndex--;
            playAudio();
        }
    }

    private void playNextAudio() {
        if (currentIndex < audioFiles.size() - 1) {
            currentIndex++;
            playAudio();
        }
    }

    private void retrieveAudioFiles() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media._ID};

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long audioId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                Uri audioUri = Uri.withAppendedPath(uri, String.valueOf(audioId));
                audioFiles.add(audioUri);
            }
            cursor.close();
        }

        loadFavoriteAudioFromDatabase();

        updateUI();
    }

    private void loadFavoriteAudioFromDatabase() {
        List<String> favoriteAudioPaths = databaseHelper.getAllFavoriteAudio();
        for (String path : favoriteAudioPaths) {
            Uri audioUri = Uri.parse(path);
            favoriteAudioFiles.add(audioUri);
        }
    }

    private boolean checkPermission() {
        int readExternalStoragePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return readExternalStoragePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                retrieveAudioFiles();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access audio files.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        playButton.setText(mediaPlayer.isPlaying() ? "Pause" : "Play");
        previousButton.setEnabled(currentIndex > 0);
        nextButton.setEnabled(currentIndex < audioFiles.size() - 1);

        if (!audioFiles.isEmpty()) {
            Uri audioUri = audioFiles.get(currentIndex);
            String audioFileName = getAudioFileName(audioUri);
            audioFileNameTextView.setText(audioFileName);
        } else {
            audioFileNameTextView.setText("");
        }
    }

    private String getAudioFileName(Uri audioUri) {
        String[] projection = {MediaStore.Audio.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(audioUri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            String audioFileName = cursor.getString(nameColumnIndex);
            cursor.close();
            return audioFileName;
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    public void downloadClicked(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Download Link");

        // Create an EditText view for the user to enter the download link
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set the positive button to initiate the download
        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String downloadLink = input.getText().toString();
                downloadAudio(downloadLink);
            }
        });

        // Set the negative button to cancel the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void downloadAudio(String downloadLink) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadLink));

        // Extract the file name from the download link
        String fileName = getFileNameFromUrl(downloadLink);

        // Set the desired file name
        request.setTitle(fileName);
        request.setDescription("Downloading audio");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Specify the destination directory explicitly
        String destinationDir = Environment.DIRECTORY_DOWNLOADS; // Use the public "Downloads" directory
        request.setDestinationInExternalPublicDir(destinationDir, fileName);

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to start download", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show a toast message with the downloaded file path
        String downloadedFilePath = Environment.getExternalStoragePublicDirectory(destinationDir).getPath() + "/" + fileName;
        Toast.makeText(this, "Download complete! File saved at: " + downloadedFilePath, Toast.LENGTH_LONG).show();
    }




    // Helper method to extract the file name from the download link
    private String getFileNameFromUrl(String downloadLink) {
        String fileName = "audio.mp3"; // Default file name if extraction fails

        try {
            // Create a URL object from the download link
            URL url = new URL(downloadLink);

            // Get the file name from the URL's path
            String path = url.getPath();
            fileName = path.substring(path.lastIndexOf('/') + 1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return fileName;
    }




}