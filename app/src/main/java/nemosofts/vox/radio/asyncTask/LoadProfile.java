package nemosofts.vox.radio.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.ProfileListener;
import nemosofts.vox.radio.interfaces.SuccessListener;
import nemosofts.vox.radio.item.ItemUser;
import nemosofts.vox.radio.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadProfile extends AsyncTask<String, String, String> {

    private final RequestBody requestBody;
    private final ProfileListener profileListener;
    private String success = "0", message = "", userId = "", userName = "", userEmail = "", userPhone = "", userGender = "", profileImage = "";

    public LoadProfile(ProfileListener profileListener, RequestBody requestBody) {
        this.profileListener = profileListener;
        this.requestBody = requestBody;
    }

    @Override
    protected void onPreExecute() {
        profileListener.onStart();
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
                userId = c.getString("user_id");
                userName = c.getString("user_name");
                userEmail = c.getString("user_email");
                userPhone = c.getString("user_phone");
                userGender = c.getString("user_gender");
                profileImage = c.getString("profile_img");
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        profileListener.onEnd(s, success, message, userId, userName, userEmail, userPhone, userGender, profileImage);
        super.onPostExecute(s);
    }
}