package com.example.wifidirectchatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fileTransfer.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_FILES = "files";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PATH = "path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FILES_TABLE = "CREATE TABLE " + TABLE_FILES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_PATH + " TEXT"
                + ")";
        db.execSQL(CREATE_FILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FILES);
        onCreate(db);
    }

    public void addFile(FileModel file) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, file.getName());
        values.put(COLUMN_PATH, file.getPath());

        db.insert(TABLE_FILES, null, values);
        db.close();
    }

    public List<FileModel> getAllFiles() {
        List<FileModel> fileList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FILES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            // Print column names for debugging
            String[] columnNames = cursor.getColumnNames();
            for (String columnName : columnNames) {
                System.out.println("Column: " + columnName);
            }

            if (cursor.moveToFirst()) {
                do {
                    FileModel file = new FileModel();
                    // Check and retrieve column index
                    int idIndex = cursor.getColumnIndex(COLUMN_ID);
                    int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                    int pathIndex = cursor.getColumnIndex(COLUMN_PATH);

                    if (idIndex >= 0 && nameIndex >= 0 && pathIndex >= 0) {
                        file.setId(cursor.getInt(idIndex));
                        file.setName(cursor.getString(nameIndex));
                        file.setPath(cursor.getString(pathIndex));
                    } else {
                        System.out.println("One or more columns not found.");
                    }

                    fileList.add(file);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }
        return fileList;
    }
}
