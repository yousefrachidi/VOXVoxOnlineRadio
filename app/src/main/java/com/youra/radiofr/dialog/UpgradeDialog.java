package com.youra.radiofr.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.youra.radiofr.R;
import com.youra.radiofr.callback.Callback;

public class UpgradeDialog {

    private final Dialog dialog;

    public UpgradeDialog(@NonNull Activity activity) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app_upgrade);
        dialog.setCancelable(false);

        TextView tv_upgrade_desc = dialog.findViewById(R.id.tv_upgrade_desc);
        tv_upgrade_desc.setText(Callback.app_update_desc);

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.iv_close).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_do).setOnClickListener(view -> {
            dismissDialog();
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Callback.app_redirect_url)));
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
