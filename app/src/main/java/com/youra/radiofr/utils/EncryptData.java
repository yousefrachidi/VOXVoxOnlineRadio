package com.youra.radiofr.utils;

import android.content.Context;

import com.yakivmospan.scytale.Crypto;
import com.yakivmospan.scytale.Options;
import com.yakivmospan.scytale.Store;

import javax.crypto.SecretKey;

import com.youra.radiofr.BuildConfig;

public class EncryptData {

   private final SecretKey key;

    public EncryptData(Context context) {
        Store store = new Store(context);
        if (!store.hasKey(BuildConfig.ENC_KEY)) {
            key = store.generateSymmetricKey(BuildConfig.ENC_KEY, null);
        } else {
            key = store.getSymmetricKey(BuildConfig.ENC_KEY, null);
        }
    }

    public String encrypt(String value) {
        try {
            Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
            return crypto.encrypt(value, key);
        } catch (Exception e) {
            Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
            return crypto.encrypt("null", key);
        }
    }

    public String decrypt(String value) {
        try {
            Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
            return crypto.decrypt(value, key);
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }
}
