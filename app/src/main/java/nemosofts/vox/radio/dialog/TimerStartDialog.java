package nemosofts.vox.radio.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.shawnlin.numberpicker.NumberPicker;

import nemosofts.vox.radio.R;

public class TimerStartDialog {

    private Dialog dialog;
    private final Activity ctx;
    private final TimerStartListener listener;

    public TimerStartDialog(@NonNull Activity ctx, TimerStartListener listener) {
        this.ctx = ctx;
        this.listener = listener;
    }

    public void showDialog() {
        dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_time_pickers);

        NumberPicker hours_picker = dialog.findViewById(R.id.hours_picker);
        NumberPicker minute_picker = dialog.findViewById(R.id.minute_picker);

        dialog.findViewById(R.id.iv_close_timer).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_cancel_timer).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_start_timer).setOnClickListener(view -> {
            String hours = String.valueOf(hours_picker.getValue());
            String minute = String.valueOf(minute_picker.getValue());
            dismissDialog();
            listener.onStart(hours, minute);
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

    public interface TimerStartListener {
        void onStart(String hours, String minute);
    }
}
