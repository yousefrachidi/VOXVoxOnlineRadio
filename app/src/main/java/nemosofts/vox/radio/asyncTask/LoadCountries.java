package nemosofts.vox.radio.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.CountriesListener;
import nemosofts.vox.radio.item.ItemCountries;
import nemosofts.vox.radio.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadCountries extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final CountriesListener countriesListener;
    private final ArrayList<ItemCountries> arrayList = new ArrayList<>();
    private String verifyStatus = "0", message = "";

    public LoadCountries(CountriesListener countriesListener, RequestBody requestBody) {
        this.countriesListener = countriesListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        countriesListener.onStart();
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
                    String id = objJson.getString(Callback.TAG_COU_ID);
                    String title = objJson.getString(Callback.TAG_COU_NAME);
                    String image = objJson.getString(Callback.TAG_COU_IMAGE);
                    String imageThumb = objJson.getString(Callback.TAG_COU_IMAGE_THUMB);
                    String totalPost = objJson.getString("total_post");

                    ItemCountries objItem = new ItemCountries(id, title, image, imageThumb, totalPost);
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
        countriesListener.onEnd(s, verifyStatus, message, arrayList);
        super.onPostExecute(s);
    }
}