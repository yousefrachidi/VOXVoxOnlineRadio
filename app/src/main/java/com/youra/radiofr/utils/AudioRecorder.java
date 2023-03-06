package com.youra.radiofr.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;

import com.youra.radiofr.R;
import com.youra.radiofr.callback.Callback;

public class AudioRecorder {

    private final Context context;
    String LOG_TAG = "TAG_ofcurrentdata";

    public AudioRecorder(Context context) {
        this.context = context;
    }

    public void onStartRecord() {
        try {
            new Record().execute();
            Callback.isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class Record extends AsyncTask<Void, Void, StringBuilder> {

        public Record() {
            // this constructor is empty
        }

        @RequiresApi(api = Build.VERSION_CODES.S)
        protected StringBuilder doInBackground(Void... voids) {
            try {
                final int n = new Random().nextInt(1048) + 2048;
                String random = "recording-" + n;

                String iconsStoragePath = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_MUSIC + File.separator + context.getString(R.string.app_name);
                File sdIconStorageDir = new File(iconsStoragePath);
                if (!sdIconStorageDir.exists()) {
                    sdIconStorageDir.mkdirs();
                }
                String os = sdIconStorageDir + "/" + random + ".mp3";
                Callback.inputStream = new URL(Callback.arrayList_play.get(Callback.playPos).getRadioUrl()).openStream();
                Log.d(LOG_TAG, "url.openStream()");
                Callback.fileOutputStream = new FileOutputStream(os);
                String stringBuilder = "FileOutputStream: " + os;
                Log.d(LOG_TAG, stringBuilder);

                while (true) {
                    int l;
                    byte[] buffer = new byte[256];
                    Log.d("buffer.print", "" + Arrays.toString(buffer));
                    while ((l = Callback.inputStream.read(buffer)) != -1) {
                        Log.d(LOG_TAG, "in while" + Arrays.toString(buffer));
                        Log.d(LOG_TAG, "in while" + l);
                        Callback.fileOutputStream.write(buffer, 0, l);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                StringBuilder stringBuilder3 = new StringBuilder();
                Log.d(LOG_TAG, "in catch" + e);
                stringBuilder3.append("");
                return stringBuilder3;
            }
        }

        @Override
        @SuppressLint({"WrongConstant", "ShowToast"})
        protected void onPostExecute(StringBuilder s) {
            super.onPostExecute(s);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Done");
            stringBuilder.append(s);
            context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(new File(String.valueOf(s)))));
        }
    }

    public static void onStopRecord() {
        try {
            Callback.isRecording = false;
            if (Callback.fileOutputStream != null) {
                Callback.fileOutputStream.flush();
                Callback.fileOutputStream.close();
            }

            Callback.inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Boolean getIsRecord() {
        return Callback.isRecording;
    }
}