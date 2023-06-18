package com.example.tp8redo;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AudioPlayerService extends Service implements MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private Uri audioUri;
    private String audioFileName;

    private final IBinder binder = new AudioPlayerBinder();

    private static final String ACTION_PLAY = "com.example.tp8redo.ACTION_PLAY";
    private static final String ACTION_PAUSE = "com.example.tp8redo.ACTION_PAUSE";
    private static final String ACTION_PREVIOUS = "com.example.tp8redo.ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "com.example.tp8redo.ACTION_NEXT";
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "audio_player_channel";



    public class AudioPlayerBinder extends Binder {
        AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Audio Player Channel";
            String description = "Channel for Audio Player notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_PLAY)) {
                audioUri = intent.getParcelableExtra(Constants.EXTRA_AUDIO_URI);
                audioFileName = intent.getStringExtra("hello?");
                startAudio();
            } else if (intent.getAction().equals(ACTION_PAUSE)) {
                pauseAudio();
            } else if (intent.getAction().equals(ACTION_PREVIOUS)) {
                // Handle previous audio action
            } else if (intent.getAction().equals(ACTION_NEXT)) {
                // Handle next audio action
            }
        }

        return START_STICKY;



    }

    private void startAudio() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), audioUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

            startForeground(NOTIFICATION_ID, createNotification());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updateNotification();
        }
    }

    private void resumeAudio() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updateNotification();
        }
    }

    private Notification createNotification() {
        Intent playIntent = new Intent(this, AudioPlayerService.class);
        playIntent.setAction(ACTION_PAUSE);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, 0);

        Intent previousIntent = new Intent(this, AudioPlayerService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0, previousIntent, 0);

        Intent nextIntent = new Intent(this, AudioPlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, 0);

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Now playing")
                .setContentText(audioFileName)
                .setSmallIcon(R.drawable.play)
                .addAction(R.drawable.previous, "Previous", previousPendingIntent)
                .addAction(R.drawable.pause, "Pause", playPendingIntent)
                .addAction(R.drawable.next, "Next", nextPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    private void updateNotification() {
        Notification notification = createNotification();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopForeground(true);
        stopSelf();
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("PlayPause")) {
                if(mediaPlayer.isPlaying()) {mediaPlayer.pause();}
                else {mediaPlayer.start();}
            }
        }
    }


}