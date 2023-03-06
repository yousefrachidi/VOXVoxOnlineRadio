package com.youra.radiofr.activity;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.youra.radiofr.R;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.item.ItemCategory;
import com.youra.radiofr.utils.ApplicationUtil;
import com.youra.radiofr.utils.AudioRecorder;
import com.youra.radiofr.utils.DBHelper;
import com.youra.radiofr.utils.GlobalBus;
import com.youra.radiofr.utils.Helper;
import com.youra.radiofr.utils.MediaButtonIntentReceiver;
import com.youra.radiofr.utils.MessageEvent;
import com.youra.radiofr.utils.ParserM3UToURL;

public class PlayerService extends IntentService implements Player.Listener {

    public static final String ACTION_TOGGLE = "action.ACTION_TOGGLE";
    public static final String ACTION_TOGGLE_W = "action.ACTION_TOGGLE_W";
    public static final String ACTION_PLAY = "action.ACTION_PLAY";
    public static final String ACTION_NEXT = "action.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "action.ACTION_PREVIOUS";
    public static final String ACTION_STOP = "action.ACTION_STOP";
    public static final String ACTION_SEEKTO = "action.ACTION_SEEKTO";

    public static ExoPlayer exoPlayer = null;
    NotificationManager mNotificationManager;
    MediaSessionCompat mMediaSession;
    NotificationCompat.Builder notification;
    RemoteViews bigViews, smallViews;

    DefaultBandwidthMeter bandwidthMeter;

    Helper helper;
    DBHelper dbHelper;
    static PlayerService playerService;

    Boolean isNewSong = false;
    Bitmap bitmap;
    ComponentName componentName;
    AudioManager mAudioManager;
    PowerManager.WakeLock mWakeLock;

    public PlayerService() {
        super(null);
    }

    public static PlayerService getInstance() {
        if (playerService == null) {
            playerService = new PlayerService();
        }
        return playerService;
    }

    @NonNull
    public static Boolean getIsPlayling() {
        return exoPlayer != null && exoPlayer.getPlayWhenReady();
    }

    public long getDuration() {
        if(exoPlayer == null) {
            return 0;
        } else {
            return exoPlayer.getDuration();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //  document why this method is empty
    }

    @Override
    public void onCreate() {

        helper = new Helper(getApplicationContext());
        dbHelper = new DBHelper(getApplicationContext());

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        componentName = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(componentName);

        try {
            registerReceiver(onCallIncome, new IntentFilter("android.intent.action.PHONE_STATE"));
            registerReceiver(onHeadPhoneDetect, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        } catch (Exception e) {
            e.printStackTrace();
        }

        bandwidthMeter = new DefaultBandwidthMeter();
        exoPlayer = new ExoPlayer.Builder(getApplicationContext()).build();
        exoPlayer.addListener(this);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build();
        exoPlayer.setAudioAttributes(audioAttributes, true);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        try {
            String action = intent.getAction();
            switch (action) {
                case ACTION_TOGGLE_W:
                    togglePlay2();
                    break;
                case ACTION_PLAY:
                    startNewRadio();
                    break;
                case ACTION_TOGGLE:
                    togglePlay();
                    break;
                case ACTION_SEEKTO:
                    seekTo(intent.getExtras().getLong("seekto"));
                    break;
                case ACTION_STOP:
                    stop(intent);
                    break;
                case ACTION_PREVIOUS:
                    if (helper.isNetworkAvailable()) {
                        previous();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ACTION_NEXT:
                    if (helper.isNetworkAvailable()) {
                        next();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }

    private void startNewRadio() {
        isNewSong = true;
        setBuffer(true);
        GlobalBus.getBus().postSticky(Callback.arrayList_play.get(Callback.playPos));
        changeSongName(getString(R.string.unknown_song));
        Callback.songName = getString(R.string.unknown_song);
        Callback.isTogglePlay = false;
        isRecord();

        String url;
        try {
            url = Callback.arrayList_play.get(Callback.playPos).getRadioUrl();
            MediaSource mediaSource;

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "vox_radio"), bandwidthMeter);

            if (url.contains(".m3u8")) {
                mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(url)));
            } else if (url.contains(".m3u") || url.contains("yp.shoutcast.com/sbin/tunein-station.m3u?id=")) {
                url = ParserM3UToURL.parse(url, "m3u");

                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(url)));

            } else if (url.contains(".pls") || url.contains("listen.pls?sid=") || url.contains("yp.shoutcast.com/sbin/tunein-station.pls?id=")) {
                url = ParserM3UToURL.parse(url, "pls");

                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(url)));

            } else {
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(url)));
            }
            exoPlayer.setMediaSource(mediaSource);

            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try {
            if (Callback.isRadio) {
                dbHelper.addToRecent(Callback.arrayList_play.get(Callback.playPos));
            }else {
                dbHelper.addToRecentEpisode(Callback.arrayList_play.get(Callback.playPos));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void togglePlay2() {
        if (Callback.isPlayed) {
            if (exoPlayer.getPlayWhenReady()) {
                exoPlayer.setPlayWhenReady(false);
                Callback.isTogglePlay = false;
                isRecord();
            } else {
                exoPlayer.setPlayWhenReady(true);
                Callback.isTogglePlay = true;
            }
            changePlayPause(exoPlayer.getPlayWhenReady());
            updateNotPlay(exoPlayer.getPlayWhenReady());
        }else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.not_start_fm), Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlay() {
        if (exoPlayer.getPlayWhenReady()) {
            Callback.isTogglePlay = false;
            isRecord();
        } else {
            Callback.isTogglePlay = true;
        }
        exoPlayer.setPlayWhenReady(!exoPlayer.getPlayWhenReady());
        changePlayPause(exoPlayer.getPlayWhenReady());
        updateNotPlay(exoPlayer.getPlayWhenReady());
    }

    private void previous() {
        setBuffer(true);
        if (Callback.playPos > 0) {
            Callback.playPos = Callback.playPos - 1;
        } else {
            Callback.playPos = Callback.arrayList_play.size() - 1;
        }
        if (Callback.isPurchases){
            startNewRadio();
        }else {
            if (Callback.arrayList_play.get(Callback.playPos).IsPremium()){
                previous();
            }else {
                startNewRadio();
            }
        }
    }

    private void next() {
        setBuffer(true);
        if (Callback.playPos < (Callback.arrayList_play.size() - 1)) {
            Callback.playPos = Callback.playPos + 1;
        } else {
            Callback.playPos = 0;
        }
        if (Callback.isPurchases){
            startNewRadio();
        }else {
            if (Callback.arrayList_play.get(Callback.playPos).IsPremium()){
                next();
            }else {
                startNewRadio();
            }
        }
    }

    private void seekTo(long seek) {
        exoPlayer.seekTo((int) seek);
    }

    private void onCompletion() {
        next();
        startNewRadio();
    }

    private void changePlayPause(Boolean flag) {
        try {
            changeEquilizer();
            GlobalBus.getBus().postSticky(new MessageEvent(flag, "playicon"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBuffer(Boolean isBuffer) {
        if (!isBuffer) {
            changeEquilizer();
        }
        GlobalBus.getBus().postSticky(new MessageEvent(isBuffer, "buffer"));
    }

    private void changeEquilizer() {
        GlobalBus.getBus().postSticky(new ItemCategory("", "", "", "",""));
    }

    private void stop(Intent intent) {
        try {
            isRecord();
            Callback.isPlayed = false;
            Callback.isTogglePlay = false;
            exoPlayer.setPlayWhenReady(false);
            changePlayPause(false);
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
            try {
                mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                mAudioManager.unregisterMediaButtonEventReceiver(componentName);
                unregisterReceiver(onCallIncome);
                unregisterReceiver(onHeadPhoneDetect);
            } catch (Exception e) {
                e.printStackTrace();
            }
            stopService(intent);
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void createNoti() {
        bigViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        smallViews = new RemoteViews(getPackageName(), R.layout.layout_noti_small);

        Intent notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.putExtra("isnoti", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent previousIntent = new Intent(this, PlayerService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(ACTION_TOGGLE);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent closeIntent = new Intent(this, PlayerService.class);
        closeIntent.setAction(ACTION_STOP);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, PendingIntent.FLAG_IMMUTABLE);

        String NOTIFICATION_CHANNEL_ID = "radio_ch_1";
        notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle(getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_audiotrack)
                .setTicker(Callback.arrayList_play.get(Callback.playPos).getRadioTitle())
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setOnlyAlertOnce(true);

        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(mChannel);

            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            PendingIntent pendingIntentMedia = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent,     PendingIntent.FLAG_IMMUTABLE);
            mMediaSession = new MediaSessionCompat(getApplicationContext(), getString(R.string.app_name), null, pendingIntentMedia);

            mMediaSession.setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

            mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, Callback.arrayList_play.get(Callback.playPos).getRadioTitle())
                    .build());

            notification.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mMediaSession.getSessionToken())
                            .setShowCancelButton(true)
                            .setShowActionsInCompactView(0, 1, 2))
                    .addAction(new NotificationCompat.Action(
                            R.drawable.ic_skip_previous, "Previous",
                            ppreviousIntent))
                    .addAction(new NotificationCompat.Action(
                            R.drawable.ic_pause, "Pause",
                            pplayIntent))
                    .addAction(new NotificationCompat.Action(
                            R.drawable.ic_skip_next, "Next",
                            pnextIntent))
                    .addAction(new NotificationCompat.Action(
                            R.drawable.ic_close_white, "Close",
                            pcloseIntent));

            notification.setContentTitle(Callback.arrayList_play.get(Callback.playPos).getRadioTitle());
            if (Callback.isRadio) {
                notification.setContentText(Callback.songName);
            } else {
                notification.setContentText(Callback.arrayList_play.get(Callback.playPos).getCategoryName());
            }
        } else {
            bigViews.setOnClickPendingIntent(R.id.imageView_noti_play, pplayIntent);

            bigViews.setOnClickPendingIntent(R.id.imageView_noti_next, pnextIntent);

            bigViews.setOnClickPendingIntent(R.id.imageView_noti_prev, ppreviousIntent);

            bigViews.setOnClickPendingIntent(R.id.imageView_noti_close, pcloseIntent);
            smallViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

            bigViews.setImageViewResource(R.id.imageView_noti_play, android.R.drawable.ic_media_pause);

            bigViews.setTextViewText(R.id.textView_noti_name, Callback.arrayList_play.get(Callback.playPos).getRadioTitle());
            smallViews.setTextViewText(R.id.status_bar_track_name, Callback.arrayList_play.get(Callback.playPos).getRadioTitle());

            if (Callback.isRadio) {
                bigViews.setTextViewText(R.id.textView_noti_artist, Callback.songName);
                smallViews.setTextViewText(R.id.status_bar_artist_name, Callback.songName);
            } else {
                bigViews.setTextViewText(R.id.textView_noti_artist, Callback.arrayList_play.get(Callback.playPos).getCategoryName());
                smallViews.setTextViewText(R.id.status_bar_artist_name, Callback.arrayList_play.get(Callback.playPos).getCategoryName());
            }
            notification.setCustomContentView(smallViews).setCustomBigContentView(bigViews);
        }
        startForeground(101, notification.build());
        updateNotiImage();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateNotiImage() {
        new AsyncTask<String, String, String>() {

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }

            @Override
            protected String doInBackground(String... strings) {
                try {
                    if (Callback.isRadio){
                        ApplicationUtil.responsePost(Callback.API_URL, helper.callAPI(Callback.METHOD_RADIO_ID,0, Callback.arrayList_play.get(Callback.playPos).getId(),"","","","","","","","", "","", "", null));
                    }
                    getBitmapFromURL(Callback.arrayList_play.get(Callback.playPos).getImage());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notification.setLargeIcon(bitmap);
                    } else {
                        bigViews.setImageViewBitmap(R.id.imageView_noti, bitmap);
                        smallViews.setImageViewBitmap(R.id.status_bar_album_art, bitmap);
                    }
                    mNotificationManager.notify(101, notification.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private void updateNot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setContentTitle(Callback.arrayList_play.get(Callback.playPos).getRadioTitle());
            if (Callback.isRadio) {
                notification.setContentText(Callback.songName);
            }else {
                notification.setContentText(Callback.arrayList_play.get(Callback.playPos).getCategoryName());
            }

            mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, Callback.arrayList_play.get(Callback.playPos).getRadioTitle())
                    .build());
        } else {
            bigViews.setTextViewText(R.id.textView_noti_name, Callback.arrayList_play.get(Callback.playPos).getRadioTitle());
            smallViews.setTextViewText(R.id.status_bar_track_name, Callback.arrayList_play.get(Callback.playPos).getRadioTitle());
            if (Callback.isRadio) {
                bigViews.setTextViewText(R.id.textView_noti_artist, Callback.songName);
                smallViews.setTextViewText(R.id.status_bar_artist_name, Callback.songName);
            }else {
                bigViews.setTextViewText(R.id.textView_noti_artist, Callback.arrayList_play.get(Callback.playPos).getCategoryName());
                smallViews.setTextViewText(R.id.status_bar_artist_name, Callback.arrayList_play.get(Callback.playPos).getCategoryName());
            }
        }
        updateNotiImage();
        updateNotPlay(exoPlayer.getPlayWhenReady());
    }

    @SuppressLint("RestrictedApi")
    private void updateNotPlay(Boolean isPlay) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification.mActions.remove(1);
                Intent playIntent = new Intent(this, PlayerService.class);
                playIntent.setAction(ACTION_TOGGLE);
                PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);
                if (isPlay) {
                    notification.mActions.add(1, new NotificationCompat.Action(
                            R.drawable.ic_pause, "Pause",
                            ppreviousIntent));

                } else {
                    notification.mActions.add(1, new NotificationCompat.Action(
                            R.drawable.ic_play, "Play",
                            ppreviousIntent));
                }
            } else {
                if (isPlay) {
                    bigViews.setImageViewResource(R.id.imageView_noti_play, android.R.drawable.ic_media_pause);
                } else {
                    bigViews.setImageViewResource(R.id.imageView_noti_play, android.R.drawable.ic_media_play);
                }
            }
            mNotificationManager.notify(101, notification.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Player.Listener.super.onPlaybackStateChanged(playbackState);
        if (playbackState == Player.STATE_READY) {
            exoPlayer.play();
            if (isNewSong) {
                isNewSong = false;
                Callback.isTogglePlay = true;
                Callback.isPlayed = true;
                setBuffer(false);
                GlobalBus.getBus().postSticky(Callback.arrayList_play.get(Callback.playPos));
                if (notification == null) {
                    createNoti();
                } else {
                    updateNotiImage();
                }
            }
        } else if (playbackState == Player.STATE_ENDED) {
            onCompletion();
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        Player.Listener.super.onIsPlayingChanged(isPlaying);
        changePlayPause(isPlaying);
        if (isPlaying) {
            if (!mWakeLock.isHeld()) {
                mWakeLock.acquire(60000);
            }
        } else {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    private void getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            InputStream input;
            if(Callback.API_URL.contains("https://")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            }
            bitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        Player.Listener.super.onPlayerError(error);
        exoPlayer.setPlayWhenReady(false);
        Callback.isTogglePlay = false;
        setBuffer(false);
        changePlayPause(false);
    }

    private void changeSongName(String songName) {
        Callback.songName = songName;
        GlobalBus.getBus().postSticky(new MessageEvent(true, "songName"));
    }

    BroadcastReceiver onCallIncome = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String a = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            try {
                if (exoPlayer.getPlayWhenReady()) {
                    if (a.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) || a.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        exoPlayer.setPlayWhenReady(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    };

    BroadcastReceiver onHeadPhoneDetect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (exoPlayer.getPlayWhenReady()) {
                    togglePlay();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                try {
                    if (exoPlayer.getPlayWhenReady()) {
                        togglePlay();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
                break;
        }
    };

    @Override
    public void onDestroy() {
        try {
            if (Callback.isAppOpen){
                GlobalBus.getBus().postSticky(Callback.arrayList_play.get(Callback.playPos));
            }
            isRecord();
            Callback.isTogglePlay = false;
            if(mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            exoPlayer.stop();
            exoPlayer.release();
            try {
                mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
                unregisterReceiver(onCallIncome);
                unregisterReceiver(onHeadPhoneDetect);
                mAudioManager.unregisterMediaButtonEventReceiver(componentName);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void isRecord(){
        try {
            AudioRecorder.onStopRecord();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}