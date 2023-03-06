package com.youra.radiofr.interfaces;

public interface DialogListener {
    void onShow();
    void onDismiss(String success, String message);
}