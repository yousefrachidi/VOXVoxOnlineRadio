package com.youra.radiofr.utils;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


public class RecorderPlayer {

    private final Context ctx;
    private DefaultBandwidthMeter bandwidthMeter;
    public static ExoPlayer exoPlayerRecorder = null;

    @NonNull
    public static Boolean getIsPlaying() {
        return exoPlayerRecorder != null && exoPlayerRecorder.getPlayWhenReady();
    }

    public RecorderPlayer(Context ctx) {
        this.ctx = ctx;
    }

    public void onCreate() {
        bandwidthMeter = new DefaultBandwidthMeter();

        exoPlayerRecorder = new ExoPlayer.Builder(ctx).build();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        exoPlayerRecorder.setAudioAttributes(audioAttributes, true);
    }

    public void setUrl(String finalUrl) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(ctx, Util.getUserAgent(ctx, "nemosofts_rc"), bandwidthMeter);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(finalUrl)));

        exoPlayerRecorder.setMediaSource(mediaSource);
        exoPlayerRecorder.prepare();
        exoPlayerRecorder.setPlayWhenReady(true);
    }

    public static void onStopAudio() {
        try {
            exoPlayerRecorder.stop();
            exoPlayerRecorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}