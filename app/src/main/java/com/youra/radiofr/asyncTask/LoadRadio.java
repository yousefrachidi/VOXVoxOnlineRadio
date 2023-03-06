package com.youra.radiofr.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.RadioListener;
import com.youra.radiofr.item.ItemRadio;
import com.youra.radiofr.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadRadio extends AsyncTask<String, String, String> {

    private final RadioListener songsListener;
    private final ArrayList<ItemRadio> arrayList = new ArrayList<>();
    private final RequestBody requestBody;
    private String verifyStatus = "0", message = "";

    public LoadRadio(RadioListener songsListener, RequestBody requestBody) {
        this.songsListener = songsListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        songsListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected  String doInBackground(String... strings)  {
        String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
        try {
            JSONObject jOb = new JSONObject(json);
            JSONArray jsonArray = jOb.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                if (!objJson.has(Callback.TAG_SUCCESS)) {

                    String id = objJson.getString(Callback.TAG_RADIO_ID);
                    String catId = objJson.getString(Callback.TAG_RADIO_CAT_ID);
                    String radioTitle = objJson.getString(Callback.TAG_RADIO_TITLE);
                    String radioURL = objJson.getString(Callback.TAG_RADIO_URL);
                    String image = objJson.getString(Callback.TAG_RADIO_IMAGE);
                    String imageThumb = objJson.getString(Callback.TAG_RADIO_IMAGE_THUMB);
                    String averageRating = objJson.getString(Callback.TAG_RADIO_AVG_RATE);
                    String totalRate = objJson.getString(Callback.TAG_RADIO_TOTAL_RATE);
                    String catName = objJson.getString(Callback.TAG_RADIO_CAT_NAME);
                    String total_views = objJson.getString(Callback.TAG_RADIO_VIEWS);
                    boolean isPremium = objJson.getBoolean(Callback.TAG_IS_PREM);
                    boolean isFav = objJson.getBoolean(Callback.TAG_IS_FAV);

                    ItemRadio objItem = new ItemRadio(id, catId, radioTitle, radioURL, image, imageThumb,averageRating,totalRate,total_views,catName,isPremium,isFav);
                    arrayList.add(objItem);

                } else {
                    verifyStatus = objJson.getString(Callback.TAG_SUCCESS);
                    message = objJson.getString(Callback.TAG_MSG);
                }
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        songsListener.onEnd(s, verifyStatus, message, arrayList);
        super.onPostExecute(s);
    }

}

