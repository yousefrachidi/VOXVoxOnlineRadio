package com.youra.radiofr.callback;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.youra.radiofr.BuildConfig;
import com.youra.radiofr.item.ItemAbout;
import com.youra.radiofr.item.ItemBanner;
import com.youra.radiofr.item.ItemRadio;
import com.youra.radiofr.item.ItemRecorder;

public class Callback implements Serializable {
    private static final long serialVersionUID = 1L;

    // API URL
    public static String API_URL = BuildConfig.BASE_URL+"api.php";

    public static Context context;
    public static Boolean isAppOpen = false;
    public static String search_item = "";

    // RadioData
    public static ArrayList<ItemRadio> arrayList_play = new ArrayList<>();
    public static int playPos = 0;
    public static String songName = "";
    public static Boolean isPlayed = false;
    public static Boolean isTogglePlay = false;
    public static Boolean isNewAdded = true;
    public static Boolean isRadio = true;
    public static String addedFrom = "";

    // RecordingData
    public static ArrayList<ItemRecorder> arrayList_play_rc = new ArrayList<>();
    public static int playPos_rc = 0;
    public static boolean isRecording = false;
    public static InputStream inputStream;
    public static FileOutputStream fileOutputStream;

    // CallBacks
    public static ArrayList<ItemRadio> arrayList_latest = new ArrayList<>();
    public static ArrayList<ItemRadio> arrayList_most = new ArrayList<>();
    public static ArrayList<ItemBanner> arrayList_banner = new ArrayList<>();

    // TAG_API
    public static final String TAG_ROOT = "VOX_RADIO_APP";
    public static final String TAG_SUCCESS = "success";
    public static final String TAG_MSG = "MSG";

    // Method
    public static final String METHOD_APP_DETAILS = "app_details";
    public static final String METHOD_LOGIN = "user_login";
    public static final String METHOD_REGISTER = "user_register";
    public static final String METHOD_PROFILE = "user_profile";
    public static final String METHOD_ACCOUNT_DELETE = "account_delete";
    public static final String METHOD_EDIT_PROFILE = "edit_profile";
    public static final String METHOD_USER_IMAGES_UPDATE = "user_images_update";
    public static final String METHOD_FORGOT_PASSWORD = "forgot_pass";
    public static final String METHOD_NOTIFICATION = "get_notification";
    public static final String METHOD_REMOVE_NOTIFICATION = "remove_notification";
    public static final String METHOD_SUGGESTION = "post_suggest";
    public static final String METHOD_PLAN = "subscription_list";
    public static final String TRANSACTION_URL = "transaction";

    public static final String METHOD_REPORT = "post_report";
    public static final String METHOD_GET_RATINGS = "get_rating";
    public static final String METHOD_RATINGS = "post_rating";
    public static final String METHOD_RADIO_ID = "radio_id";
    public static final String METHOD_DO_FAV = "favourite_post";
    public static final String METHOD_POST_BY_FAV = "get_favourite";

    public static final String METHOD_HOME = "home";
    public static final String METHOD_CAT = "get_cat_list";
    public static final String METHOD_COU = "get_countries_list";
    public static final String METHOD_LATEST = "get_latest";
    public static final String METHOD_MOST_VIEWED = "get_most_viewed";
    public static final String METHOD_RADIO_BY_RECENT = "get_recent_radio";
    public static final String METHOD_SEARCH = "radio_search";

    public static final String METHOD_CAT_ID = "get_cat_by";
    public static final String METHOD_COU_ID = "get_countries_by";
    public static final String METHOD_POST_BY_BANNER = "get_banner_by";

    public static final String METHOD_HOME_PODCASTS = "home_podcasts";
    public static final String METHOD_PODCASTS = "get_podcasts";
    public static final String METHOD_PODCASTS_ID = "get_podcasts_by";

    // TAG
    public static final String TAG_USER_ID = "user_id";
    public static final String TAG_AUTH_ID = "auth_id";
    public static final String TAG_USER_NAME = "user_name";
    public static final String TAG_USER_EMAIL = "user_email";
    public static final String TAG_USER_PHONE = "user_phone";
    public static final String TAG_USER_GENDER = "user_gender";
    public static final String TAG_USER_IMAGE = "profile_img";

    public static final String TAG_RADIO_ID = "id";
    public static final String TAG_RADIO_CAT_ID = "cat_id";
    public static final String TAG_RADIO_TITLE = "radio_title";
    public static final String TAG_RADIO_URL = "radio_url";
    public static final String TAG_RADIO_IMAGE = "image";
    public static final String TAG_RADIO_IMAGE_THUMB = "image_thumb";
    public static final String TAG_RADIO_AVG_RATE = "averageRating";
    public static final String TAG_RADIO_TOTAL_RATE = "totalRate";
    public static final String TAG_RADIO_CAT_NAME = "category_name";
    public static final String TAG_RADIO_VIEWS = "total_views";
    public static final String TAG_IS_PREM = "isPremium";
    public static final String TAG_IS_FAV = "isFavourite";

    public static final String TAG_CAT_ID = "cid";
    public static final String TAG_CAT_NAME = "category_name";
    public static final String TAG_CAT_IMAGE = "category_image";
    public static final String TAG_CAT_IMAGE_THUMB = "category_image_thumb";

    public static final String TAG_COU_ID = "sid";
    public static final String TAG_COU_NAME = "countries_name";
    public static final String TAG_COU_IMAGE = "countries_image";
    public static final String TAG_COU_IMAGE_THUMB = "countries_image_thumb";

    public static final String TAG_BAN_ID = "bid";
    public static final String TAG_BAN_NAME = "banner_title";
    public static final String TAG_BAN_INFO = "banner_info";
    public static final String TAG_BAN_IMAGE = "banner_image";

    public static Boolean isDarkMode = false, isProfileUpdate = false;
    public static int isDarkModeTheme = 0;

    public static final String LOGIN_TYPE_NORMAL = "Normal";
    public static final String LOGIN_TYPE_GOOGLE = "Google";

    // About Details
    public static ItemAbout itemAbout = new ItemAbout("","","","","","");

    public static int recentLimit = 10;

    public static Boolean isBannerAd = true, isInterAd = true, isNativeAd = true;

    public static int adCount = 0;
    public static int interstitialAdShow = 5, nativeAdShow = 6;

    public static String adNetwork = "admob";
    public static String ironAdsId = "";
    public static String startappAppId = "";
    public static String wortiseAppId = "";
    public static String publisherAdID = "";
    public static String bannerAdID = "";
    public static String interstitialAdID = "";
    public static String nativeAdID = "";

    public static final String AD_TYPE_ADMOB = "admob", AD_TYPE_FACEBOOK = "facebook",
            AD_TYPE_STARTAPP = "startapp", AD_TYPE_APPLOVIN = "applovins",
            AD_TYPE_IRONSOURCE = "iron", AD_TYPE_WORTISE = "wortise";

    public static Boolean isAdsLimits = true;
    public static int adCountClick = 15;

    public static Boolean isCustomAds = false;
    public static int customAdCount = 0, customAdShow = 12;
    public static String custom_ads_img = "", custom_ads_link = "";

    public static Boolean isRTL = false, isVPN = false, isAPK = false, isMaintenance = false,
            isScreenshot = false, isLogin = false, isGoogleLogin = false;

    public static Boolean isAppUpdate = false;
    public static int app_new_version = 1;
    public static String app_update_desc = "", app_redirect_url = "";

    public static Boolean isPurchases = false;
}