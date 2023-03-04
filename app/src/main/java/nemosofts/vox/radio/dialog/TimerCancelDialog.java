package nemosofts.vox.radio.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.asyncTask.GetRating;
import nemosofts.vox.radio.asyncTask.LoadRating;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.RatingListener;
import nemosofts.vox.radio.utils.Helper;

public class TimerCancelDialog {

    private Dialog dialog;
    private final Activity ctx;
    private final TimerCancelListener listener;

    public TimerCancelDialog(@NonNull Activity ctx, TimerCancelListener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    public void showDialog() {
        dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_time_cancel);

        dialog.findViewById(R.id.iv_close_timer).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_cancel_timer).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_stop_timer).setOnClickListener(view -> {
            listener.onStopped();
            dismissDialog();
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

    public interface TimerCancelListener {
        void onStopped();
    }
}
