package nemosofts.vox.radio.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.LoginListener;
import nemosofts.vox.radio.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadLogin extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final LoginListener listener;
    private String success = "0", message = "";
    private String user_id="", user_name = "", user_gender = "", profile_img = "", user_phone = "";

    public LoadLogin(LoginListener listener, RequestBody requestBody) {
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
            JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                success = jsonObject.getString(Callback.TAG_SUCCESS);
                if(success.equals("1")) {
                    user_id = jsonObject.getString(Callback.TAG_USER_ID);
                    user_name = jsonObject.getString(Callback.TAG_USER_NAME);
                    user_phone = jsonObject.getString(Callback.TAG_USER_PHONE);
                    user_gender = jsonObject.getString(Callback.TAG_USER_GENDER);
                    profile_img = jsonObject.getString(Callback.TAG_USER_IMAGE);
                }
                message = jsonObject.getString(Callback.TAG_MSG);
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s, success, message, user_id, user_name, user_gender, user_phone, profile_img);
        super.onPostExecute(s);
    }

}