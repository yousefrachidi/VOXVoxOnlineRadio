package com.youra.radiofr.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinSdk;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.wortise.ads.WortiseSdk;
import com.wortise.ads.banner.BannerAd;
import com.wortise.ads.interstitial.InterstitialAd;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

import com.youra.radiofr.R;
import com.youra.radiofr.activity.CustomAdsActivity;
import com.youra.radiofr.activity.LoginActivity;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.InterAdListener;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class Helper {

    private final Context ctx;
    private InterAdListener interAdListener;

    public Helper(Context ctx) {
        this.ctx = ctx;
    }

    public Helper(Context ctx, InterAdListener interAdListener) {
        this.ctx = ctx;
        this.interAdListener = interAdListener;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void initializeAds() {
        if (AdSupported.isAdmobFBAds()) {
            MobileAds.initialize(ctx, initializationStatus -> {
            });
        }

        if (AdSupported.isStartAppAds()) {
            StartAppSDK.init(ctx, Callback.startappAppId, false);
            StartAppAd.disableSplash();
        }

        if (AdSupported.isApplovinAds()) {
            if (!AppLovinSdk.getInstance(ctx).isInitialized()) {
                AppLovinSdk.initializeSdk(ctx);
                AppLovinSdk.getInstance(ctx).setMediationProvider("max");
                AppLovinSdk.getInstance(ctx).getSettings().setTestDeviceAdvertisingIds(Arrays.asList("656822d9-18de-4120-994e-44d4245a4d63", "249d75a2-1ef2-8ff9-8885-c50384843a66"));
            }
        }

        if (AdSupported.isWortiseAds() && !WortiseSdk.isInitialized()) {
            WortiseSdk.initialize(ctx, ctx.getString(R.string.wortise_app_id));
        }

        if (AdSupported.isIronSourceAds()) {
            IronSource.init((Activity) ctx, Callback.ironAdsId, () -> {
            });
        }

    }

    private AdView showPersonalizedAds(LinearLayout linearLayout) {
        AdView adView = new AdView(ctx);
        AdRequest adRequest;

        if(Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB)) {
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                    .build();
        } else {
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                    .addNetworkExtrasBundle(FacebookMediationAdapter.class, new Bundle())
                    .build();
        }

        adView.setAdUnitId(Callback.bannerAdID);
        adView.setAdSize(AdSize.BANNER);
        linearLayout.addView(adView);

        adView.loadAd(adRequest);
        return adView;
    }

    private AdView showNonPersonalizedAds(LinearLayout linearLayout) {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        AdView adView = new AdView(ctx);
        AdRequest adRequest;

        if(Callback.adNetwork.equals(Callback.AD_TYPE_ADMOB)) {
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
        } else {
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                    .build();
        }
        adView.setAdUnitId(Callback.bannerAdID);
        adView.setAdSize(AdSize.BANNER);
        linearLayout.addView(adView);
        adView.loadAd(adRequest);
        return adView;
    }

    public Object showBannerAd(LinearLayout linearLayout) {
        if (isNetworkAvailable() && Callback.isBannerAd) {
            switch (Callback.adNetwork) {
                case Callback.AD_TYPE_ADMOB:
                case Callback.AD_TYPE_FACEBOOK:
                    if (ConsentInformation.getInstance(ctx).getConsentStatus() == ConsentStatus.NON_PERSONALIZED) {
                        return showNonPersonalizedAds(linearLayout);
                    } else {
                        return showPersonalizedAds(linearLayout);
                    }
                case Callback.AD_TYPE_STARTAPP:
                    Banner startAppBanner = new Banner(ctx);
                    startAppBanner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(startAppBanner);
                    startAppBanner.loadAd();
                    return startAppBanner;
                case Callback.AD_TYPE_APPLOVIN:
                    MaxAdView adView = new MaxAdView(Callback.bannerAdID, ctx);
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int heightPx = ctx.getResources().getDimensionPixelSize(R.dimen.banner_height);
                    adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    linearLayout.addView(adView);
                    adView.loadAd();
                    return adView;
                case Callback.AD_TYPE_WORTISE:
                    BannerAd mBannerAd = new BannerAd(ctx);
                    mBannerAd.setAdSize(com.wortise.ads.AdSize.HEIGHT_50);
                    mBannerAd.setAdUnitId(Callback.bannerAdID);
                    linearLayout.addView(mBannerAd);
                    mBannerAd.loadAd();
                    return mBannerAd;
                case Callback.AD_TYPE_IRONSOURCE:
                    IronSourceBannerLayout iBannerAd  = IronSource.createBanner((Activity) ctx, ISBannerSize.BANNER);
                    linearLayout.addView(iBannerAd);
                    IronSource.loadBanner(iBannerAd);
                    return iBannerAd;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public void showInterAd(final int pos, final String type) {
        Callback.customAdCount = Callback.customAdCount + 1;
        if (Callback.isCustomAds && Callback.customAdCount % Callback.customAdShow == 0){
            ctx.startActivity(new Intent(ctx, CustomAdsActivity.class));
        }
        else if (Callback.isInterAd) {
            Callback.adCount = Callback.adCount + 1;
            if (Callback.adCount % Callback.interstitialAdShow == 0) {
                switch (Callback.adNetwork) {
                    case Callback.AD_TYPE_ADMOB:
                        final AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(ctx);
                        if (adManagerInterAdmob.getAd() != null) {
                            adManagerInterAdmob.getAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdManagerInterAdmob.setAd(null);
                                    adManagerInterAdmob.createAd();
                                    interAdListener.onClick(pos, type);
                                    super.onAdDismissedFullScreenContent();
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull @NotNull com.google.android.gms.ads.AdError adError) {
                                    AdManagerInterAdmob.setAd(null);
                                    adManagerInterAdmob.createAd();
                                    interAdListener.onClick(pos, type);
                                    super.onAdFailedToShowFullScreenContent(adError);
                                }
                            });
                            adManagerInterAdmob.getAd().show((Activity) ctx);
                        } else {
                            AdManagerInterAdmob.setAd(null);
                            adManagerInterAdmob.createAd();
                            interAdListener.onClick(pos, type);
                        }
                        break;
                    case Callback.AD_TYPE_STARTAPP:
                        final AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(ctx);
                        if (adManagerInterStartApp.getAd() != null && adManagerInterStartApp.getAd().isReady()) {
                            adManagerInterStartApp.getAd().showAd(new AdDisplayListener() {
                                @Override
                                public void adHidden(Ad ad) {
                                    AdManagerInterStartApp.setAd(null);
                                    adManagerInterStartApp.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void adDisplayed(Ad ad) {

                                }

                                @Override
                                public void adClicked(Ad ad) {

                                }

                                @Override
                                public void adNotDisplayed(Ad ad) {
                                    AdManagerInterStartApp.setAd(null);
                                    adManagerInterStartApp.createAd();
                                    interAdListener.onClick(pos, type);
                                }
                            });
                        } else {
                            AdManagerInterStartApp.setAd(null);
                            adManagerInterStartApp.createAd();
                            interAdListener.onClick(pos, type);
                        }
                        break;
                    case Callback.AD_TYPE_APPLOVIN:
                        final AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(ctx);
                        if (adManagerInterApplovin.getAd() != null && adManagerInterApplovin.getAd().isReady()) {
                            adManagerInterApplovin.getAd().setListener(new MaxAdListener() {
                                @Override
                                public void onAdLoaded(MaxAd ad) {

                                }

                                @Override
                                public void onAdDisplayed(MaxAd ad) {

                                }

                                @Override
                                public void onAdHidden(MaxAd ad) {
                                    AdManagerInterApplovin.setAd(null);
                                    adManagerInterApplovin.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onAdClicked(MaxAd ad) {

                                }

                                @Override
                                public void onAdLoadFailed(String adUnitId, MaxError error) {
                                    AdManagerInterApplovin.setAd(null);
                                    adManagerInterApplovin.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                    AdManagerInterApplovin.setAd(null);
                                    adManagerInterApplovin.createAd();
                                    interAdListener.onClick(pos, type);
                                }
                            });
                            adManagerInterApplovin.getAd().showAd();
                        } else {
                            AdManagerInterStartApp.setAd(null);
                            adManagerInterApplovin.createAd();
                            interAdListener.onClick(pos, type);
                        }
                        break;

                    case Callback.AD_TYPE_WORTISE:
                        final AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(ctx);
                        if (adManagerInterWortise.getAd() != null && adManagerInterWortise.getAd().isAvailable()) {
                            adManagerInterWortise.getAd().setListener(new InterstitialAd.Listener() {
                                @Override
                                public void onInterstitialClicked(@NonNull InterstitialAd interstitialAd) {

                                }

                                @Override
                                public void onInterstitialDismissed(@NonNull InterstitialAd interstitialAd) {
                                    AdManagerInterWortise.setAd(null);
                                    adManagerInterWortise.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onInterstitialFailed(@NonNull InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                    AdManagerInterWortise.setAd(null);
                                    adManagerInterWortise.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onInterstitialLoaded(@NonNull InterstitialAd interstitialAd) {

                                }

                                @Override
                                public void onInterstitialShown(@NonNull InterstitialAd interstitialAd) {

                                }
                            });
                            adManagerInterWortise.getAd().showAd();
                        } else {
                            AdManagerInterWortise.setAd(null);
                            adManagerInterWortise.createAd();
                            interAdListener.onClick(pos, type);
                        }
                        break;
                    case Callback.AD_TYPE_IRONSOURCE:
                        if (IronSource.isInterstitialReady()) {
                            IronSource.setInterstitialListener(new InterstitialListener() {
                                @Override
                                public void onInterstitialAdReady() {

                                }

                                @Override
                                public void onInterstitialAdLoadFailed(IronSourceError error) {
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onInterstitialAdOpened() {

                                }

                                @Override
                                public void onInterstitialAdClosed() {
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onInterstitialAdShowFailed(IronSourceError error) {
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onInterstitialAdClicked() {
                                }

                                @Override
                                public void onInterstitialAdShowSucceeded() {

                                }
                            });
                            IronSource.showInterstitial();
                        }else {
                            interAdListener.onClick(pos, type);
                        }
                        IronSource.init((Activity) ctx, Callback.ironAdsId, IronSource.AD_UNIT.INTERSTITIAL);
                        IronSource.loadInterstitial();
                        break;
                }
            } else {
                interAdListener.onClick(pos, type);
            }
        } else {
            interAdListener.onClick(pos, type);
        }
    }


    public RequestBody callAPI(String helper_name, int page, String itemID, String catID, String searchText, String reportMessage, String userID, String name, String email, String mobile, String gender, String password, String authID, String loginType, File file) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create();
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(gson);
        jsObj.addProperty("helper_name", helper_name);
        jsObj.addProperty("application_id", "com.devp20apps.dvradiofr");

        if (Callback.METHOD_APP_DETAILS.equals(helper_name)){
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_LOGIN.equals(helper_name)){
            jsObj.addProperty("user_email", email);
            jsObj.addProperty("user_password", password);
            jsObj.addProperty("auth_id", authID);
            jsObj.addProperty("type", loginType);
        } else if (Callback.METHOD_REGISTER.equals(helper_name)){
            jsObj.addProperty("user_name", name);
            jsObj.addProperty("user_email", email);
            jsObj.addProperty("user_phone", mobile);
            jsObj.addProperty("user_gender", gender);
            jsObj.addProperty("user_password", password);
            jsObj.addProperty("auth_id", authID);
            jsObj.addProperty("type", loginType);
        } else if (Callback.METHOD_PROFILE.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_ACCOUNT_DELETE.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_EDIT_PROFILE.equals(helper_name)){
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("user_name", name);
            jsObj.addProperty("user_email", email);
            jsObj.addProperty("user_phone", mobile);
            jsObj.addProperty("user_password", password);
        } else if (Callback.METHOD_USER_IMAGES_UPDATE.equals(helper_name)){
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("type", loginType);
        } else if (Callback.METHOD_FORGOT_PASSWORD.equals(helper_name)){
            jsObj.addProperty("user_email", email);
        } else if (Callback.METHOD_NOTIFICATION.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_REMOVE_NOTIFICATION.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_REPORT.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("report_title", searchText);
            jsObj.addProperty("report_msg", reportMessage);
        } else if (Callback.METHOD_GET_RATINGS.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("device_id", userID);
        } else if (Callback.METHOD_RATINGS.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("device_id", userID);
            jsObj.addProperty("rate", authID);
            jsObj.addProperty("message", reportMessage);
        }  else if (Callback.METHOD_SUGGESTION.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("suggest_title", searchText);
            jsObj.addProperty("suggest_message", reportMessage);
        } else if (Callback.TRANSACTION_URL.equals(helper_name)){
            jsObj.addProperty("planId", itemID);
            jsObj.addProperty("planName", catID);
            jsObj.addProperty("planPrice", searchText);
            jsObj.addProperty("planDuration", reportMessage);
            jsObj.addProperty("planCurrencyCode", name);
            jsObj.addProperty("user_id", userID);
        }

        else if (Callback.METHOD_RADIO_ID.equals(helper_name)) {
            jsObj.addProperty("radio_id", itemID);
        } else if (Callback.METHOD_DO_FAV.equals(helper_name)) {
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_POST_BY_FAV.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_POST_BY_BANNER.equals(helper_name)){
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("post_id", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_COU_ID.equals(helper_name)){
            jsObj.addProperty("countries_id", catID);
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_CAT_ID.equals(helper_name)){
            jsObj.addProperty("cat_id", catID);
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_HOME.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_CAT.equals(helper_name)){
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_COU.equals(helper_name)){
            jsObj.addProperty("page", String.valueOf(page));
        }  else if (Callback.METHOD_LATEST.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_MOST_VIEWED.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_RADIO_BY_RECENT.equals(helper_name)){
            jsObj.addProperty("page", String.valueOf(page));
            jsObj.addProperty("radio_ids", itemID);
            jsObj.addProperty("user_id", userID);
        } else if (Callback.METHOD_PODCASTS.equals(helper_name)) {
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_PODCASTS_ID.equals(helper_name)){
            jsObj.addProperty("podcasts_id", itemID);
            jsObj.addProperty("page", String.valueOf(page));
        } else if (Callback.METHOD_HOME_PODCASTS.equals(helper_name)){
            jsObj.addProperty("episode_ids", itemID);
        } else if (Callback.METHOD_SEARCH.equals(helper_name)) {
            jsObj.addProperty("user_id", userID);
            jsObj.addProperty("search_text", searchText);
            jsObj.addProperty("page", String.valueOf(page));
        }

        switch (helper_name) {
            case Callback.METHOD_REGISTER:
            case Callback.METHOD_SUGGESTION:
            case Callback.METHOD_USER_IMAGES_UPDATE: {
                final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
                MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                if (file != null) {
                    builder.addFormDataPart("image_data", file.getName(), RequestBody.create(MEDIA_TYPE_PNG, file));
                }
                return builder.addFormDataPart("data", ApplicationUtil.toBase64(jsObj.toString())).build();
            }
            default:
                return new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("data", ApplicationUtil.toBase64(jsObj.toString())).build();
        }
    }

    public void clickLogin() {
        SharedPref sharePref = new SharedPref(ctx);
        if (sharePref.isLogged()) {
            logout((Activity) ctx, sharePref);
            Toast.makeText(ctx, ctx.getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(ctx, LoginActivity.class);
            intent.putExtra("from", "app");
            ctx.startActivity(intent);
        }
    }

    public void logout(Activity activity, @NonNull SharedPref sharePref) {
        if (sharePref.getLoginType().equals(Callback.LOGIN_TYPE_GOOGLE)) {
            FirebaseAuth.getInstance().signOut();
        }
        sharePref.setIsAutoLogin(false);
        sharePref.setIsLogged(false);
        sharePref.setLoginDetails("", "", "", "", "", "", "", false, "", Callback.LOGIN_TYPE_NORMAL);
        Intent intent1 = new Intent(ctx, LoginActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.putExtra("from", "");
        ctx.startActivity(intent1);
        activity.finish();
    }

    @SuppressLint("ObsoleteSdkInt")
    public String getPathImage(Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String filePath = "";
                String wholeID = DocumentsContract.getDocumentId(uri);
                String id = wholeID.split(":")[1];
                String[] column = {MediaStore.Images.Media.DATA};
                String sel = MediaStore.Images.Media._ID + "=?";
                Cursor cursor = ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);
                int columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
                return filePath;
            } else {
                if (uri == null) {
                    return null;
                }
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int column_index = cursor
                            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String data = cursor.getString(column_index);
                    cursor.close();
                    return data;
                }
                return uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (uri == null) {
                return null;
            }
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String data = cursor.getString(column_index);
                cursor.close();
                return data;
            }
            return uri.getPath();
        }
    }

    public void getVerifyDialog(String title, String message) {
        Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_verify);
        dialog.setCancelable(false);

        TextView tv_title = dialog.findViewById(R.id.tv_dialog_title);
        TextView tv_message = dialog.findViewById(R.id.tv_dialog_message);
        tv_title.setText(title);
        tv_message.setText(message);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_dialog_done).setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void getInvalidUserDialog(String message) {
        Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_verify);
        dialog.setCancelable(false);

        TextView tv_title = dialog.findViewById(R.id.tv_dialog_title);
        TextView tv_message = dialog.findViewById(R.id.tv_dialog_message);
        tv_title.setText(ctx.getString(R.string.invalid_user));
        tv_message.setText(message);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_dialog_done).setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public String milliSecondsToTimer(long milliseconds, long duration) {
        if(duration > 0) {
            String finalTimerString;
            String hourString = "";
            String secondsString;
            String minutesString = "";

            // Convert total duration into time
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            // Add hours if there
            int temp_hour = (int) (duration / (1000 * 60 * 60));
            if (temp_hour != 0) {
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

//        // return timer string
            return finalTimerString;
        } else {
            return "0:00";
        }
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        double percentage = (double) 0;
        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);
        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;
        // return percentage
        return (int) percentage;
    }

    public long getSeekFromPercentage(int percentage, long totalDuration) {
        long currentSeconds = 0;
        long totalSeconds = (int) (totalDuration / 1000);
        // calculating percentage
        currentSeconds = (percentage * totalSeconds) / 100;
        // return percentage
        return currentSeconds * 1000;
    }
}
