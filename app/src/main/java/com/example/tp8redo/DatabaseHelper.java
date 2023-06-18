package com.example.tp8redo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";
    private static final String TABLE_NAME = "favorites";
    private static final String COLUMN_AUDIO_PATH = "audio_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_AUDIO_PATH + " TEXT PRIMARY KEY)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTableQuery);
        onCreate(db);
    }

    public void addFavoriteAudio(String audioPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AUDIO_PATH, audioPath);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void removeFavoriteAudio(String audioPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_AUDIO_PATH + " = ?", new String[]{audioPath});
        db.close();
    }

    public List<String> getAllFavoriteAudio() {
        List<String> favoriteAudioPaths = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                String audioPath = cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_PATH));
                favoriteAudioPaths.add(audioPath);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return favoriteAudioPaths;
    }


}
