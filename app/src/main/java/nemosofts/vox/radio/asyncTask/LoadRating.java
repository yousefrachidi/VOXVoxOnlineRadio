package nemosofts.vox.radio.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.RatingListener;
import nemosofts.vox.radio.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadRating extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private String msg = "",  rate = "0", rateSuccess = "0";
    private final RatingListener ratingListener;

    public LoadRating(RatingListener ratingListener, RequestBody requestBody) {
        this.ratingListener = ratingListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        ratingListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
        try {
            JSONObject jOb = new JSONObject(json);
            JSONArray jsonArray = jOb.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                rateSuccess = c.getString(Callback.TAG_SUCCESS);
                msg = c.getString(Callback.TAG_MSG);
                if (c.has("rate_avg")) {
                    rate = c.getString("rate_avg");
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
        ratingListener.onEnd(String.valueOf(s), rateSuccess, msg, Integer.parseInt(rate));
        super.onPostExecute(s);
    }
}