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

public class LoadEpisode extends AsyncTask<String, String, String> {

    private final RadioListener songsListener;
    private final ArrayList<ItemRadio> arrayList = new ArrayList<>();
    private final RequestBody requestBody;
    private String verifyStatus = "0", message = "";

    public LoadEpisode(RadioListener songsListener, RequestBody requestBody) {
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

                    String id = objJson.getString("id");
                    String podcast_id = objJson.getString("podcast_id");
                    String episode_title = objJson.getString("episode_title");
                    String episode_url = objJson.getString("episode_url");

                    String podcast_name = objJson.getString("podcast_name");
                    String podcast_image = objJson.getString("podcast_image");
                    String podcast_thumb = objJson.getString("podcast_thumb");

                    ItemRadio objItem = new ItemRadio(id,podcast_id,episode_title,episode_url,podcast_image,podcast_thumb,"0","0","0",
                            podcast_name,false,false);
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

