package nemosofts.vox.radio.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

public final class PreferenceUtil {

    public static final String NOW_PLAYING_SCREEN_ID = "playing_screen_id";
    private static PreferenceUtil sInstance;
    private final SharedPreferences mPreferences;

    private PreferenceUtil(@NonNull final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public NowPlayingScreen getNowPlayingScreen() {
        int id = mPreferences.getInt(NOW_PLAYING_SCREEN_ID, 0);
        for (NowPlayingScreen nowPlayingScreen : NowPlayingScreen.values()) {
            if (nowPlayingScreen.id == id) {
                return nowPlayingScreen;
            }
        }
        return NowPlayingScreen.NORMAL;
    }

    @SuppressLint("CommitPrefEdits")
    public void setNowPlayingScreen(@NonNull NowPlayingScreen nowPlayingScreen) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(NOW_PLAYING_SCREEN_ID, nowPlayingScreen.id);
        editor.apply();
    }
}
