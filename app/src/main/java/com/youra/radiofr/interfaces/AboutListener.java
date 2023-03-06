package com.youra.radiofr.interfaces;

public interface AboutListener {
    void onStart();
    void onEnd(String success, String verifyStatus, String message);
}