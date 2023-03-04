package nemosofts.vox.radio.asyncTask;

import android.content.Context;
import android.os.AsyncTask;

import androidx.nemosofts.lk.Envato;

import org.json.JSONArray;
import org.json.JSONObject;

import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.AboutListener;
import nemosofts.vox.radio.item.ItemAbout;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.Helper;
import nemosofts.vox.radio.utils.SharedPref;

public class LoadAbout extends AsyncTask<String, String, String> {

    private final Envato envato;
    private final Helper helper;
    private final SharedPref sharedPref;
    private final AboutListener aboutListener;
    private String verifyStatus = "0", message = "";

    public LoadAbout(Context context, AboutListener aboutListener) {
        this.aboutListener = aboutListener;
        helper = new Helper(context);
        envato = new Envato(context);
        sharedPref = new SharedPref(context);
    }

    @Override
    protected void onPreExecute() {
        aboutListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, helper.callAPI(Callback.METHOD_APP_DETAILS, 0, "", "", "", "", sharedPref.getUserId(), "", "", "", "","","","", null));
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                if (!c.has(Callback.TAG_SUCCESS)) {
                    // App Details
                    String app_email = c.getString("app_email");
                    String app_author = c.getString("app_author");
                    String app_contact = c.getString("app_contact");
                    String app_website = c.getString("app_website");
                    String app_description = c.getString("app_description");
                    String app_developed_by = c.getString("app_developed_by");
                    Callback.itemAbout = new ItemAbout(app_email,app_author,app_contact,app_website,app_description,app_developed_by);

                    // Envato
                    String apikey = c.getString("envato_api_key");
                    if (!apikey.isEmpty()){
                        envato.setEnvatoKEY(apikey);
                    }

                    // API Latest Limit
                    if(!c.getString("api_latest_limit").equals("")) {
                        Callback.recentLimit = Integer.parseInt(c.getString("api_latest_limit"));
                    }

                    // Ads
                    Callback.adNetwork = c.getString("ad_network");
                    Callback.publisherAdID = c.getString("publisher_id");
                    Callback.startappAppId = c.getString("startapp_app_id");
                    Callback.ironAdsId = c.getString("iron_ads_id");
                    Callback.wortiseAppId = c.getString("wortise_app_id");

                    Callback.isBannerAd = Boolean.parseBoolean(c.getString("banner_ad"));
                    Callback.bannerAdID = c.getString("banner_ad_id");
                    Callback.isInterAd = Boolean.parseBoolean(c.getString("interstital_ad"));
                    Callback.interstitialAdID = c.getString("interstital_ad_id");
                    if(!c.getString("interstital_ad_click").equals("")) {
                        Callback.interstitialAdShow = Integer.parseInt(c.getString("interstital_ad_click"));
                    }
                    Callback.isNativeAd = Boolean.parseBoolean(c.getString("native_ad"));
                    Callback.nativeAdID = c.getString("native_ad_id");
                    if(!c.getString("native_position").equals("")) {
                        Callback.nativeAdShow = Integer.parseInt(c.getString("native_position"));
                    }

                    // AdsLimits
                    Callback.isAdsLimits = Boolean.parseBoolean(c.getString("ads_limits"));
                    Callback.adCountClick = Integer.parseInt(c.getString("ads_count_click"));

                    // CustomAds
                    Callback.isCustomAds = Boolean.parseBoolean(c.getString("custom_ads"));
                    Callback.custom_ads_img = c.getString("custom_ads_img");
                    Callback.custom_ads_link = c.getString("custom_ads_link");
                    if(!c.getString("custom_ads_clicks").equals("")) {
                        Callback.customAdShow = Integer.parseInt(c.getString("custom_ads_clicks"));
                    }

                    // is
                    Callback.isRTL = Boolean.parseBoolean(c.getString("isRTL"));
                    Callback.isVPN = Boolean.parseBoolean(c.getString("isVPN"));
                    Callback.isAPK = Boolean.parseBoolean(c.getString("isAPK"));
                    Callback.isMaintenance = Boolean.parseBoolean(c.getString("isMaintenance"));
                    Callback.isScreenshot = Boolean.parseBoolean(c.getString("isScreenshot"));
                    Callback.isLogin = Boolean.parseBoolean(c.getString("isLogin"));
                    Callback.isGoogleLogin = Boolean.parseBoolean(c.getString("isGoogleLogin"));

                    // AppUpdate
                    Callback.isAppUpdate = Boolean.parseBoolean(c.getString("app_update_status"));
                    if(!c.getString("app_new_version").equals("")) {
                        Callback.app_new_version = Integer.parseInt(c.getString("app_new_version"));
                    }
                    Callback.app_update_desc = c.getString("app_update_desc");
                    Callback.app_redirect_url = c.getString("app_redirect_url");

                } else {
                    verifyStatus = c.getString(Callback.TAG_SUCCESS);
                    message = c.getString(Callback.TAG_MSG);
                }
            }
            return "1";
        } catch (Exception ee) {
            ee.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        aboutListener.onEnd(s, verifyStatus, message);
        super.onPostExecute(s);
    }
}