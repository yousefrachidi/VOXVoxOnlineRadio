package nemosofts.vox.radio.interfaces;

import java.util.ArrayList;

import nemosofts.vox.radio.item.ItemNotification;

public interface NotificationListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemNotification> notificationArrayList);
}