package com.youra.radiofr.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.youra.radiofr.R;
import com.youra.radiofr.activity.BillingSubscribeActivity;

public class PremiumDialog {

    private final Dialog dialog;

    public PremiumDialog(@NonNull Activity activity) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium_pack);
        dialog.setCancelable(false);

        dialog.findViewById(R.id.iv_close_subscribe).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_subscribe_now).setOnClickListener(view -> {
            dismissDialog();
            Intent intent = new Intent(activity, BillingSubscribeActivity.class);
            activity.startActivity(intent);
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
