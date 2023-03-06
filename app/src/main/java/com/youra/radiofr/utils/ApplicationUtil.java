package com.youra.radiofr.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import com.youra.radiofr.R;
import com.youra.radiofr.callback.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApplicationUtil {

    public static Boolean isDarkMode() {
        return Callback.isDarkMode;
    }

    public static int isTheme() {
        return Callback.isDarkModeTheme;
    }

    @NonNull
    public static String responsePost(String url, RequestBody requestBody) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(25000, TimeUnit.MILLISECONDS)
                .writeTimeout(25000, TimeUnit.MILLISECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @NonNull
    public static String toBase64(@NonNull String input) {
        byte[] encodeValue = Base64.encode(input.getBytes(), Base64.DEFAULT);
        return new String(encodeValue);
    }


    @NonNull
    public static String viewFormat(@NonNull Integer number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    @NonNull
    public static String readableFileSize(long size) {
        if (size <= 0) return "0 Bytes";
        final String[] units = new String[]{"Bytes", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int colorUtils(Context ctx) {
        if (Callback.isDarkModeTheme == 1) {
            return ContextCompat.getColor(ctx, R.color.titleNight);
        }else if (Callback.isDarkModeTheme == 2) {
            return ContextCompat.getColor(ctx, R.color.titleGrey);
        }else if (Callback.isDarkModeTheme == 3) {
            return ContextCompat.getColor(ctx, R.color.titleBlue);
        }else {
            return ContextCompat.getColor(ctx, R.color.titleLight);
        }
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int accentColorUtils(Context ctx) {
        if (Callback.isDarkModeTheme == 1) {
            return ContextCompat.getColor(ctx, R.color.colorAccentNight);
        }else if (Callback.isDarkModeTheme == 2) {
            return ContextCompat.getColor(ctx, R.color.colorAccentGrey);
        }else if (Callback.isDarkModeTheme == 3) {
            return ContextCompat.getColor(ctx, R.color.colorAccentBlue);
        }else {
            return ContextCompat.getColor(ctx, R.color.colorAccentLight);
        }
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int accent_50ColorUtils(Context ctx) {
        if (Callback.isDarkModeTheme == 1) {
            return ContextCompat.getColor(ctx, R.color.colorAccentNight_50);
        }else if (Callback.isDarkModeTheme == 2) {
            return ContextCompat.getColor(ctx, R.color.colorAccentGrey_50);
        }else if (Callback.isDarkModeTheme == 3) {
            return ContextCompat.getColor(ctx, R.color.colorAccentBlue_50);
        }else {
            return ContextCompat.getColor(ctx, R.color.colorAccentLight_50);
        }
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int titleColorUtils(Context ctx) {
        if (Callback.isDarkModeTheme == 1) {
            return ContextCompat.getColor(ctx, R.color.titleNight);
        }else if (Callback.isDarkModeTheme == 2) {
            return ContextCompat.getColor(ctx, R.color.titleGrey);
        }else if (Callback.isDarkModeTheme == 3) {
            return ContextCompat.getColor(ctx, R.color.titleBlue);
        }else {
            return ContextCompat.getColor(ctx, R.color.titleLight);
        }
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int colorUtilsWhite(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.white);
    }

    @SuppressLint("PrivateResource")
    @ColorInt
    public static int colorUtilsBlack(Context ctx) {
        return ContextCompat.getColor(ctx, R.color.black);
    }

    @Nullable
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            InputStream input;
            if(Callback.API_URL.contains("https://")) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            } else {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                input = connection.getInputStream();
            }
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    public static GradientDrawable getGradientDrawable(int first, int second) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(15);
        gd.setColors(new int[]{first, second});
        gd.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gd.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
        gd.mutate();
        return gd;
    }

    public static int placeholderRadio() {
        if (Callback.isDarkMode) {
            return R.drawable.placeholder_song_night;
        }else {
            return R.drawable.placeholder_song_light;
        }
    }

    public static long convert_long(@NonNull String s) {
        long ms = 0;
        Pattern p;
        if (s.contains(("\\:"))) {
            p = Pattern.compile("(\\d+):(\\d+)");
        } else {
            p = Pattern.compile("(\\d+).(\\d+)");
        }
        Matcher m = p.matcher(s);
        if (m.matches()) {
            int h = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            ms = (long) h * 60 * 60 * 1000 + min * 60 * 1000;
        }
        return ms;
    }

    public static float convertDpToPixel(float dp, @NonNull Resources resources) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * metrics.density;
    }

    @NonNull
    @Contract(pure = true)
    public static String milliSecondsToTimerDownload(long milliseconds) {
        String finalTimerString = "";
        String hourString = "";
        String secondsString = "";
        String minutesString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there

        if (hours != 0) {
            hourString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        // Prepending 0 to minutes if it is one digit
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = "" + minutes;
        }

        finalTimerString = hourString + minutesString + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }
}
