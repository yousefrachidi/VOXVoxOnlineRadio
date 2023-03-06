package com.youra.radiofr.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.CategoryListener;
import com.youra.radiofr.item.ItemCategory;
import com.youra.radiofr.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadCategory extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final CategoryListener categoryListener;
    private final ArrayList<ItemCategory> arrayList = new ArrayList<>();
    private String verifyStatus = "0", message = "";

    public LoadCategory(CategoryListener categoryListener, RequestBody requestBody) {
        this.categoryListener = categoryListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        categoryListener.onStart();
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
                    String id = objJson.getString(Callback.TAG_CAT_ID);
                    String title = objJson.getString(Callback.TAG_CAT_NAME);
                    String image = objJson.getString(Callback.TAG_CAT_IMAGE);
                    String imageThumb = objJson.getString(Callback.TAG_CAT_IMAGE_THUMB);
                    String totalPost = objJson.getString("total_post");

                    ItemCategory objItem = new ItemCategory(id, title, image, imageThumb,totalPost);
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
        categoryListener.onEnd(s, verifyStatus, message, arrayList);
        super.onPostExecute(s);
    }
}