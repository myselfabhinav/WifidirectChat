package com.example.wifidirectchatapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FileTransferActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileModel> fileList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_transfer);

        Button btnSelectFile = findViewById(R.id.btn_select_file);
        recyclerView = findViewById(R.id.rv_files);

        fileList = new ArrayList<>();
        fileAdapter = new FileAdapter(this, fileList); // Pass context and fileList
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileAdapter);

        databaseHelper = new DatabaseHelper(this);

        // Load files from the database and display them
        loadFiles();

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileSelector();
            }
        });
    }

    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String fileName = getFileName(uri);
                String filePath = uri.toString();

                // Save file to the database
                databaseHelper.addFile(new FileModel(0, fileName, filePath));
                loadFiles(); // Refresh the list
            }
        }
    }

    private void loadFiles() {
        fileList.clear();
        fileList.addAll(databaseHelper.getAllFiles());
        fileAdapter.notifyDataSetChanged();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    // Get the index of the DISPLAY_NAME column
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    // Check if the column index is valid
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
