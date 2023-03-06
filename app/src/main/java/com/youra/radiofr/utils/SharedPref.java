package com.youra.radiofr.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.youra.radiofr.callback.Callback;

public class SharedPref {

    private final EncryptData encryptData;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private static final String TAG_FIRST_OPEN = "firstopen", TAG_IS_LOGGED = "islogged", TAG_UID = "uid", TAG_USERNAME = "name",
            TAG_EMAIL = "email", TAG_MOBILE = "mobile", TAG_GENDER = "gender", TAG_REMEMBER = "rem" ,
            TAG_PASSWORD = "pass", SHARED_PREF_AUTOLOGIN = "autologin", TAG_LOGIN_TYPE = "loginType",
            TAG_AUTH_ID = "auth_id", TAG_IMAGES = "profile";

    private static final String TAG_PUBLISHER_ID = "publisher_id ", TAG_STARTAPP_ID = "startapp_id",
            TAG_IRON_ID = "iron_id",TAG_WORTISE_ID= "wortise_id",  TAG_AD_IS_BANNER = "isbanner",
            TAG_AD_IS_INTER = "isinter", TAG_AD_IS_NATIVE = "isnative", TAG_AD_NETWORK = "ad_network",
            TAG_AD_ID_BANNER = "id_banner", TAG_AD_ID_INTER = "id_inter", TAG_AD_ID_NATIVE = "id_native",
            TAG_AD_NATIVE_POS = "native_pos", TAG_AD_INTER_POS = "inter_pos";

    private static final String TAG_NIGHT_MODE = "night_mode", TAG_THEME = "my_theme";

    public SharedPref(Context ctx) {
        encryptData = new EncryptData(ctx);
        sharedPreferences = ctx.getSharedPreferences("setting_app", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setIsFirst(Boolean flag) {
        editor.putBoolean(TAG_FIRST_OPEN, flag);
        editor.apply();
    }

    public Boolean getIsFirst() {
        return sharedPreferences.getBoolean(TAG_FIRST_OPEN, true);
    }

    public void setIsLogged(Boolean isLogged) {
        editor.putBoolean(TAG_IS_LOGGED, isLogged);
        editor.apply();
    }

    public boolean isLogged() {
        return sharedPreferences.getBoolean(TAG_IS_LOGGED, false);
    }

    public void setLoginDetails(String id, String name, String mobile, String email, String gender,
                                @NonNull String profilePic, String authID, Boolean isRemember, String password, String loginType) {
        editor.putBoolean(TAG_REMEMBER, isRemember);
        editor.putString(TAG_UID, encryptData.encrypt(id));
        editor.putString(TAG_USERNAME, encryptData.encrypt(name));
        editor.putString(TAG_MOBILE, encryptData.encrypt(mobile));
        editor.putString(TAG_EMAIL, encryptData.encrypt(email));
        editor.putString(TAG_GENDER, encryptData.encrypt(gender));
        editor.putString(TAG_PASSWORD, encryptData.encrypt(password));
        editor.putString(TAG_LOGIN_TYPE, encryptData.encrypt(loginType));
        editor.putString(TAG_AUTH_ID, encryptData.encrypt(authID));
        editor.putString(TAG_IMAGES, encryptData.encrypt(profilePic.replace(" ", "%20")));
        editor.apply();
    }

    public void setRemember(Boolean isRemember) {
        editor.putBoolean(TAG_REMEMBER, isRemember);
        editor.putString(TAG_PASSWORD, "");
        editor.apply();
    }

    public String getUserId() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_UID, ""));
    }

    public void setUserName(String userName) {
        editor.putString(TAG_USERNAME, encryptData.encrypt(userName));
        editor.apply();
    }

    public String getUserName() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_USERNAME, ""));
    }

    public void setEmail(String email) {
        editor.putString(TAG_EMAIL, encryptData.encrypt(email));
        editor.apply();
    }

    public String getEmail() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_EMAIL,""));
    }

    public void setUserMobile(String mobile) {
        editor.putString(TAG_MOBILE, encryptData.encrypt(mobile));
        editor.apply();
    }

    public String getUserMobile() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_MOBILE, ""));
    }

    public String getPassword() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_PASSWORD,""));
    }

    public Boolean getIsAutoLogin() { return sharedPreferences.getBoolean(SHARED_PREF_AUTOLOGIN, false); }

    public void setIsAutoLogin(Boolean isAutoLogin) {
        editor.putBoolean(SHARED_PREF_AUTOLOGIN, isAutoLogin);
        editor.apply();
    }

    public Boolean getIsRemember() {
        return sharedPreferences.getBoolean(TAG_REMEMBER, false);
    }


    public String getLoginType() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_LOGIN_TYPE,""));
    }

    public String getAuthID() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_AUTH_ID,""));
    }
    public String getProfileImages() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_IMAGES,""));
    }

    public void setProfileImages(@NonNull String profile_img) {
        editor.putString(TAG_IMAGES, encryptData.encrypt(profile_img.replace(" ", "%20")));
        editor.apply();
    }

    public void setAdsDetails(boolean isBanner, boolean isInter, boolean isNative, String adNetwork,
                              String publisher_id, String startapp_id,String iron_id, String wortise_id,
                              String banner_id, String inter_id, String native_id,
                              int interPos, int nativePos) {
        editor.putString(TAG_AD_NETWORK, adNetwork);
        editor.putBoolean(TAG_AD_IS_BANNER, isBanner);
        editor.putBoolean(TAG_AD_IS_INTER, isInter);
        editor.putBoolean(TAG_AD_IS_NATIVE, isNative);

        editor.putString(TAG_PUBLISHER_ID, publisher_id);
        editor.putString(TAG_STARTAPP_ID, startapp_id);
        editor.putString(TAG_IRON_ID, iron_id);
        editor.putString(TAG_WORTISE_ID, wortise_id);

        editor.putString(TAG_AD_ID_BANNER, banner_id);
        editor.putString(TAG_AD_ID_INTER, inter_id);
        editor.putString(TAG_AD_ID_NATIVE, native_id);

        editor.putInt(TAG_AD_INTER_POS, interPos);
        editor.putInt(TAG_AD_NATIVE_POS, nativePos);
        editor.apply();
    }

    public void getAdDetails() {
        Callback.adNetwork = sharedPreferences.getString(TAG_AD_NETWORK, Callback.AD_TYPE_ADMOB);

        Callback.publisherAdID = sharedPreferences.getString(TAG_PUBLISHER_ID, "");
        Callback.startappAppId = sharedPreferences.getString(TAG_STARTAPP_ID, "");
        Callback.ironAdsId = sharedPreferences.getString(TAG_IRON_ID, "");
        Callback.wortiseAppId = sharedPreferences.getString(TAG_WORTISE_ID, "");

        Callback.bannerAdID = sharedPreferences.getString(TAG_AD_ID_BANNER, "");
        Callback.interstitialAdID = sharedPreferences.getString(TAG_AD_ID_INTER, "");
        Callback.nativeAdID = sharedPreferences.getString(TAG_AD_ID_NATIVE, "");

        Callback.interstitialAdShow = sharedPreferences.getInt(TAG_AD_INTER_POS, 5);
        Callback.nativeAdShow = sharedPreferences.getInt(TAG_AD_NATIVE_POS, 6);
    }

    public void setDarkMode(Boolean state) {
        editor.putBoolean(TAG_NIGHT_MODE, state);
        editor.apply();
    }

    public void setModeTheme(int state) {
        editor.putInt(TAG_THEME, state);
        editor.apply();
    }
    public int getModeTheme() {
        return sharedPreferences.getInt(TAG_THEME, 0);
    }

    public void getThemeDetails() {
        Callback.isDarkMode = sharedPreferences.getBoolean(TAG_NIGHT_MODE, false);
        Callback.isDarkModeTheme = sharedPreferences.getInt(TAG_THEME, 0);
    }

    public Boolean getIsNotification() {
        return sharedPreferences.getBoolean("noti", true);
    }

    public void setIsNotification(Boolean isNotification) {
        editor.putBoolean("noti", isNotification);
        editor.apply();
    }

//    ---------------------------------------------

    public void setCheckSleepTime() {
        if (getSleepTime() <= System.currentTimeMillis()) {
            setSleepTime(false, 0, 0);
        }
    }

    public void setSleepTime(Boolean isTimerOn, long sleepTime, int id) {
        editor.putBoolean("isTimerOn", isTimerOn);
        editor.putLong("sleepTime", sleepTime);
        editor.putInt("sleepTimeID", id);
        editor.apply();
    }

    public Boolean getIsSleepTimeOn() {
        return sharedPreferences.getBoolean("isTimerOn", false);
    }

    public long getSleepTime() {
        return sharedPreferences.getLong("sleepTime", 0);
    }

    public int getSleepID() {
        return sharedPreferences.getInt("sleepTimeID", 0);
    }

    public Boolean isSnowFall() { return sharedPreferences.getBoolean("switch_snow_fall", false); }
    public void setSnowFall(Boolean state) {
        editor.putBoolean("switch_snow_fall", state);
        editor.apply();
    }

    public Boolean isVolume() { return sharedPreferences.getBoolean("switch_volume", true); }
    public void setVolume(Boolean state) {
        editor.putBoolean("switch_volume", state);
        editor.apply();
    }

    public int getBlurAmount() {
        return sharedPreferences.getInt("blur_amount", 20);
    }
    public void setBlurAmount(int state) {
        editor.putInt("blur_amount", state);
        editor.apply();
    }

    public Boolean isScaleBanner() { return sharedPreferences.getBoolean("switch_scale", true); }
    public void setScaleBanner(Boolean state) {
        editor.putBoolean("switch_scale", state);
        editor.apply();
    }

    public int getBlurAmountDrive() {
        return sharedPreferences.getInt("blur_amount_drive", 5);
    }
    public void setBlurAmountDrive(int state) {
        editor.putInt("blur_amount_drive", state);
        editor.apply();
    }

    public Boolean isDriveColor() { return sharedPreferences.getBoolean("switch_color_drive", false); }
    public void setDriveColor(Boolean state) {
        editor.putBoolean("switch_color_drive", state);
        editor.apply();
    }


}