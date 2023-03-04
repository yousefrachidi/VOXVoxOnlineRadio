package nemosofts.vox.radio.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.SocialLoginListener;
import nemosofts.vox.radio.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadRegister extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final SocialLoginListener socialLoginListener;
    private String success = "0", message = "";
    private String user_id = "", user_name = "", email = "", auth_id = "";

    public LoadRegister(SocialLoginListener socialLoginListener, RequestBody requestBody) {
        this.socialLoginListener = socialLoginListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        socialLoginListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            JSONObject mainJson = new JSONObject(json);
            JSONArray jsonArray = mainJson.getJSONArray(Callback.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);

                success = c.getString(Callback.TAG_SUCCESS);
                message = c.getString(Callback.TAG_MSG);
                if(c.has(Callback.TAG_USER_ID)) {
                    user_id = c.getString(Callback.TAG_USER_ID);
                    user_name = c.getString(Callback.TAG_USER_NAME);
                    auth_id = c.getString(Callback.TAG_AUTH_ID);
                    email = c.getString(Callback.TAG_USER_EMAIL);
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
        socialLoginListener.onEnd(s, success, message, user_id, user_name, email, auth_id);
        super.onPostExecute(s);
    }
}