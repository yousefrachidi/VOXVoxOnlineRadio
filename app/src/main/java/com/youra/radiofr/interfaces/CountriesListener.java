package com.youra.radiofr.interfaces;

import java.util.ArrayList;

import com.youra.radiofr.item.ItemCountries;

public interface CountriesListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message, ArrayList<ItemCountries> arrayListCountries);
}