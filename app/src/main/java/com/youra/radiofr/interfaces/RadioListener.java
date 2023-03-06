package com.youra.radiofr.interfaces;

import java.util.ArrayList;

import com.youra.radiofr.item.ItemRadio;

public interface RadioListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemRadio> arrayListRadio);
}