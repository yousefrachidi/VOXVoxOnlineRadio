package com.youra.radiofr.interfaces;

import java.util.ArrayList;

import com.youra.radiofr.item.ItemCategory;

public interface CategoryListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCategory> arrayListCat);
}