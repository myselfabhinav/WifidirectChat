package com.example.wifidirectchatapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<FileModel> fileList;
    private Context context;

    public FileAdapter(Context context, List<FileModel> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileModel file = fileList.get(position);
        holder.fileName.setText(file.getName());

        holder.btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadFileTask(context, holder.progressBar).execute(file.getPath());
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class FileViewHolder extends RecyclerView.ViewHolder {

        TextView fileName;
        Button btnDownload;
        ProgressBar progressBar;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            btnDownload = itemView.findViewById(R.id.btn_download);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    private static class DownloadFileTask extends AsyncTask<String, Integer, File> {
        private Context context;
        private ProgressBar progressBar;

        public DownloadFileTask(Context context, ProgressBar progressBar) {
            this.context = context;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected File doInBackground(String... strings) {
            String fileUrl = strings[0];
            File downloadedFile = null;

            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                InputStream inputStream = connection.getInputStream();
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                String fileName = Uri.parse(fileUrl).getLastPathSegment();
                downloadedFile = new File(downloadsDir, fileName);

                FileOutputStream outputStream = new FileOutputStream(downloadedFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                long fileLength = connection.getContentLength();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    publishProgress((int) (totalBytesRead * 100 / fileLength));
                }

                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return downloadedFile;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            progressBar.setVisibility(View.GONE);
            if (file != null && file.exists()) {
                // Notify the user that the file has been downloaded
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = Uri.fromFile(file);
                intent.setDataAndType(fileUri, "*/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(intent, "Open file"));

                // Optionally, notify the user that the file is saved in Downloads folder
                Toast.makeText(context, "File downloaded to Downloads folder", Toast.LENGTH_SHORT).show();
            } else {
                // Handle the case where the download fails
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
