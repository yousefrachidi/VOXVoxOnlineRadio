package nemosofts.vox.radio.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.HomeListener;
import nemosofts.vox.radio.item.ItemBanner;
import nemosofts.vox.radio.item.ItemRadio;
import nemosofts.vox.radio.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadHome extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final HomeListener listener;
    private final ArrayList<ItemRadio> arrayList_latest = new ArrayList<>();
    private final ArrayList<ItemRadio> arrayList_most = new ArrayList<>();
    private final ArrayList<ItemBanner> arrayList_banner = new ArrayList<>();

    public LoadHome(HomeListener listener, RequestBody requestBody) {
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

            JSONArray jsonArrayHomeBa = jsonObject.getJSONArray("banner_data");
            for (int i = 0; i < jsonArrayHomeBa.length(); i++) {
                JSONObject objJson = jsonArrayHomeBa.getJSONObject(i);

                String bid = objJson.getString(Callback.TAG_BAN_ID);
                String bannerTitle = objJson.getString(Callback.TAG_BAN_NAME);
                String bannerInfo = objJson.getString(Callback.TAG_BAN_INFO);
                String bannerImage = objJson.getString(Callback.TAG_BAN_IMAGE);

                ItemBanner objItem = new ItemBanner(bid, bannerTitle, bannerInfo, bannerImage);
                arrayList_banner.add(objItem);
            }

            JSONArray jsonArrayHome1 = jsonObject.getJSONArray("latest_radio");
            for (int i = 0; i < jsonArrayHome1.length(); i++) {
                JSONObject objJson = jsonArrayHome1.getJSONObject(i);

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
                arrayList_latest.add(objItem);
            }

            JSONArray jsonArrayHome2 = jsonObject.getJSONArray("most_view");
            for (int i = 0; i < jsonArrayHome2.length(); i++) {
                JSONObject objJson = jsonArrayHome2.getJSONObject(i);

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
                arrayList_most.add(objItem);
            }

            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s,arrayList_banner, arrayList_latest, arrayList_most);
        super.onPostExecute(s);
    }

}