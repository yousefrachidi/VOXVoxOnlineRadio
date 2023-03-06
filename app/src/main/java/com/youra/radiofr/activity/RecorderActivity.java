package com.youra.radiofr.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import com.youra.radiofr.R;
import com.youra.radiofr.adapter.AdapterRecorder;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.ifSupported.IsRTL;
import com.youra.radiofr.ifSupported.IsScreenshot;
import com.youra.radiofr.item.ItemRecorder;
import com.youra.radiofr.utils.ApplicationUtil;
import com.youra.radiofr.utils.RecorderPlayer;

public class RecorderActivity extends YouraCompatActivity {

    private RecyclerView rv;
    private AdapterRecorder adapter;
    private ArrayList<ItemRecorder> arrayList;
    private ProgressBar progressbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        arrayList = new ArrayList<>();
        progressbar = findViewById(R.id.pb);
        rv = findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(RecorderActivity.this);
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        try {
            new LoadDownloadSongs().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_recorder;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    @SuppressLint("StaticFieldLeak")
    class LoadDownloadSongs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            if (!arrayList.isEmpty()){
                arrayList.clear();
            }
            progressbar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String iconsStoragePath = Environment.getExternalStorageDirectory().toString() + File.separator + Environment.DIRECTORY_MUSIC + File.separator + getString(R.string.app_name);
                File root = new File(iconsStoragePath);
                File[] songs = root.listFiles((dir, name) -> name.endsWith(".mp3"));
                if (songs != null) {
                    for (int i = 0; i < songs.length; i++) {
                        MediaMetadataRetriever md = new MediaMetadataRetriever();
                        md.setDataSource(songs[i].getAbsolutePath());
                        String title = songs[i].getName();
                        String duration = md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        duration = ApplicationUtil.milliSecondsToTimerDownload(Long.parseLong(duration));
                        String url = songs[i].getAbsolutePath();
                        long file_size = songs[i].length();
                        arrayList.add(new ItemRecorder(String.valueOf(i), url, title, duration, file_size));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            adapter = new AdapterRecorder(RecorderActivity.this, arrayList, (itemData, position) -> {
                Callback.arrayList_play_rc.clear();
                Callback.arrayList_play_rc.addAll(arrayList);
                Callback.playPos_rc = position;
                adapter.notifyDataSetChanged();
                if (PlayerService.exoPlayer != null && PlayerService.exoPlayer.getPlayWhenReady()) {
                    Intent intent = new Intent(getApplicationContext(), PlayerService.class);
                    intent.setAction(PlayerService.ACTION_STOP);
                    startService(intent);
                }
            });
            rv.setAdapter(adapter);
            progressbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        try {
            RecorderPlayer.onStopAudio();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        try {
            RecorderPlayer.onStopAudio();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }
}