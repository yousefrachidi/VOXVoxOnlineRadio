package nemosofts.vox.radio.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.nemosofts.lk.ProCompatActivity;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.asyncTask.LoadForgotPass;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;
import nemosofts.vox.radio.interfaces.SuccessListener;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.Helper;

public class ForgotPasswordActivity extends YouraCompatActivity {

    private Helper helper;
    private EditText editText_email;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        helper = new Helper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);

        editText_email = findViewById(R.id.et_forgot_email);

        findViewById(R.id.tv_btn_forgot_send).setOnClickListener(v -> {
            if (!editText_email.getText().toString().trim().isEmpty()) {
                loadForgotPass();
            } else {
                Toast.makeText(ForgotPasswordActivity.this, getString(R.string.error_email), Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout adView = findViewById(R.id.ll_adView);
        helper.showBannerAd(adView);
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_forgot_password;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    private void loadForgotPass() {
        if (helper.isNetworkAvailable()) {
            LoadForgotPass loadForgotPass = new LoadForgotPass(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        showDialog(message);
                        Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.callAPI(Callback.METHOD_FORGOT_PASSWORD, 0, "", "", "", "", "", "", editText_email.getText().toString(), "", "", "", "", "", null));
            loadForgotPass.execute();
        } else {
            Toast.makeText(ForgotPasswordActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialog(String message) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_verify);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView tv_title = dialog.findViewById(R.id.tv_dialog_title);
        TextView tv_message = dialog.findViewById(R.id.tv_dialog_message);
        tv_title.setText(getString(R.string.app_name));
        tv_message.setText(message);

        dialog.findViewById(R.id.iv_dialog_close).setOnClickListener(view -> {
            dialog.dismiss();
            editText_email.setText("");
        });
        dialog.findViewById(R.id.tv_dialog_done).setOnClickListener(view -> {
            dialog.dismiss();
            editText_email.setText("");
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }
}