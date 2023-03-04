package nemosofts.vox.radio.asyncTask;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.NotificationListener;
import nemosofts.vox.radio.item.ItemNotification;
import nemosofts.vox.radio.utils.ApplicationUtil;
import okhttp3.RequestBody;

public class LoadNotification extends AsyncTask<String, String, String> {

    private final NotificationListener listener;
    private final RequestBody requestBody;
    private final ArrayList<ItemNotification> arrayList = new ArrayList<>();

    public LoadNotification(NotificationListener listener, RequestBody requestBody) {
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
            JSONObject jsonObject = new JSONObject(json);

            JSONArray jsonArray = jsonObject.getJSONArray(Callback.TAG_ROOT);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj_com = jsonArray.getJSONObject(i);

                String id = obj_com.getString("id");
                String title = obj_com.getString("notification_title");
                String msg = obj_com.getString("notification_msg");
                String notification_on = obj_com.getString("notification_on");

                ItemNotification item = new ItemNotification(id, title, msg, notification_on);
                arrayList.add(item);
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        listener.onEnd(s, arrayList);
        super.onPostExecute(s);
    }
}
