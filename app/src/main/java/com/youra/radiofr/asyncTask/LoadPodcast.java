package com.youra.radiofr.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.PodcastsListener;
import com.youra.radiofr.item.ItemPodcasts;
import com.youra.radiofr.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadPodcast extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final PodcastsListener podcastsListener;
    private final ArrayList<ItemPodcasts> arrayList = new ArrayList<>();
    private String verifyStatus = "0", message = "";

    public LoadPodcast(PodcastsListener podcastsListener, RequestBody requestBody) {
        this.podcastsListener = podcastsListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        podcastsListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                if (!objJson.has(Callback.TAG_SUCCESS)) {
                    String id = objJson.getString("pid");
                    String title = objJson.getString("podcast_name");
                    String image = objJson.getString("podcast_image");
                    String imageThumb = objJson.getString("podcast_thumb");
                    String description = objJson.getString("podcast_description");

                    ItemPodcasts objItem = new ItemPodcasts(id, title, image, imageThumb, description);
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
        podcastsListener.onEnd(s, verifyStatus, message, arrayList);
        super.onPostExecute(s);
    }
}