package nemosofts.vox.radio.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.asyncTask.LoadStatus;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.interfaces.SuccessListener;
import nemosofts.vox.radio.utils.Helper;
import nemosofts.vox.radio.utils.SharedPref;

public class FeedBackDialog {

    private final Helper helper;
    private final SharedPref sharedPref;
    private Dialog dialog;
    private final Activity ctx;
    private final ProgressDialog progressDialog;

    public FeedBackDialog(@NonNull Activity ctx) {
        this.ctx = ctx;
        helper = new Helper(ctx);
        sharedPref = new SharedPref(ctx);
        progressDialog = new ProgressDialog(ctx,R.style.ThemeDialog);
        progressDialog.setMessage(ctx.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    public void showDialog(String id, String title) {
        dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_feed_back);

        EditText et_messages = dialog.findViewById(R.id.et_messages);

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.iv_close).setOnClickListener(view -> dismissDialog());
        dialog.findViewById(R.id.tv_submit).setOnClickListener(view -> {
            if(et_messages.getText().toString().trim().isEmpty()) {
                et_messages.setError(ctx.getString(R.string.please_describe_the_problem));
                et_messages.requestFocus();
            } else {
                if(sharedPref.isLogged()) {
                    loadReportSubmit(et_messages.getText().toString(), id, title);
                } else {
                    helper.clickLogin();
                }
            }
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private void loadReportSubmit(String reportMessages, String itemID, String reportTitle) {
        if (helper.isNetworkAvailable()) {
            LoadStatus loadFav = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String reportSuccess, String message) {
                    if (success.equals("1")) {
                        if (reportSuccess.equals("1")) {
                            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                    dismissDialog();
                }
            }, helper.callAPI(Callback.METHOD_REPORT, 0, itemID, "", reportTitle, reportMessages, sharedPref.getUserId(), "", "", "", "", "", "", "", null));
            loadFav.execute();
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
