package com.youra.radiofr.interfaces;

import java.util.ArrayList;

import com.youra.radiofr.item.ItemBanner;
import com.youra.radiofr.item.ItemRadio;

public interface HomeListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemBanner> arrayListBanner, ArrayList<ItemRadio> arrayListLatest, ArrayList<ItemRadio> arrayLisMost);
}