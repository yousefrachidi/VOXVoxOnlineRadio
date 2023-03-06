package com.youra.radiofr.interfaces;

import java.util.ArrayList;

import com.youra.radiofr.item.ItemNotification;

public interface NotificationListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemNotification> notificationArrayList);
}