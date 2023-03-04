package nemosofts.vox.radio.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.activity.BillingSubscribeActivity;

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
