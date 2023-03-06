package com.youra.radiofr.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.PodcastsHomeListener;
import com.youra.radiofr.item.ItemPodcasts;
import com.youra.radiofr.item.ItemRadio;
import com.youra.radiofr.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadHomePodcasts extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final PodcastsHomeListener listener;
    private final ArrayList<ItemPodcasts> arrayList_podcasts = new ArrayList<>();
    private final ArrayList<ItemRadio> arrayList_episode = new ArrayList<>();

    public LoadHomePodcasts(PodcastsHomeListener listener, RequestBody requestBody) {
        this.listener = listener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        listener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONObject jsonObject = mainJson.getJSONObject(Callback.TAG_ROOT);

            JSONArray jsonArrayHome1 = jsonObject.getJSONArray("latest_podcasts");
            for (int i = 0; i < jsonArrayHome1.length(); i++) {
                JSONObject objJson = jsonArrayHome1.getJSONObject(i);

                String id = objJson.getString("pid");
                String title = objJson.getString("podcast_name");
                String image = objJson.getString("podcast_image");
                String imageThumb = objJson.getString("podcast_thumb");
                String description = objJson.getString("podcast_description");

                ItemPodcasts objItem = new ItemPodcasts(id, title, image, imageThumb, description);
                arrayList_podcasts.add(objItem);
            }

            JSONArray jsonArrayHome2 = jsonObject.getJSONArray("episode_podcasts");
            for (int i = 0; i < jsonArrayHome2.length(); i++) {
                JSONObject objJson = jsonArrayHome2.getJSONObject(i);

                String id = objJson.getString("id");
                String podcast_id = objJson.getString("podcast_id");
                String episode_title = objJson.getString("episode_title");
                String episode_url = objJson.getString("episode_url");

                String podcast_name = objJson.getString("podcast_name");
                String podcast_image = objJson.getString("podcast_image");
                String podcast_thumb = objJson.getString("podcast_thumb");

                ItemRadio objItem = new ItemRadio(id,podcast_id,episode_title,episode_url,podcast_image,podcast_thumb,"0","0","0",
                        podcast_name,false,false);
                arrayList_episode.add(objItem);
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s, arrayList_podcasts, arrayList_episode);
        super.onPostExecute(s);
    }

}