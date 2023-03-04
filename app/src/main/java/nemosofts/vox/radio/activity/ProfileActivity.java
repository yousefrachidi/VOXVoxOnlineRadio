package nemosofts.vox.radio.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.nemosofts.lk.ProCompatActivity;

import com.squareup.picasso.Picasso;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.asyncTask.LoadProfile;
import nemosofts.vox.radio.asyncTask.LoadStatus;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;
import nemosofts.vox.radio.interfaces.ProfileListener;
import nemosofts.vox.radio.interfaces.SuccessListener;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.Helper;
import nemosofts.vox.radio.utils.SharedPref;

public class ProfileActivity extends YouraCompatActivity {

    private Helper helper;
    private SharedPref sharedPref;
    private ProgressDialog progressDialog;
    private TextView textView_name;
    private TextView textView_email;
    private TextView textView_mobile;
    private LinearLayout ll_mobile;
    private View view_phone;
    private ImageView iv_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        setLayoutPar(toolbar);

        helper = new Helper(this);
        sharedPref = new SharedPref(this);

        progressDialog = new ProgressDialog(this, R.style.ThemeDialog);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        textView_name = findViewById(R.id.tv_profile_name);
        textView_email = findViewById(R.id.tv_profile_email);
        textView_mobile = findViewById(R.id.tv_prof_mobile);
        ll_mobile = findViewById(R.id.ll_mobile);
        view_phone = findViewById(R.id.view_phone);
        iv_profile = findViewById(R.id.iv_profile);

        if (sharedPref.isLogged() && !sharedPref.getUserId().equals("")) {
            loadUserProfile();
        } else {
            helper.clickLogin();
        }

        findViewById(R.id.ll_delete_btn).setOnClickListener(view -> showDeleteDialog());
        findViewById(R.id.ll_edit_btn).setOnClickListener(view -> startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class)));

        LinearLayout adView = findViewById(R.id.ll_adView);
        helper.showBannerAd(adView);
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_profile;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    private void showDeleteDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_account);

        dialog.findViewById(R.id.iv_close).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_cancel).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.tv_do).setOnClickListener(view -> {
            dialog.dismiss();
            loadDelete();
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void loadDelete() {
        if (helper.isNetworkAvailable()) {
            LoadStatus loadStatus = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        helper.clickLogin();
                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.callAPI(Callback.METHOD_ACCOUNT_DELETE, 0, "", "", "", "",sharedPref.getUserId(), "", "", "", "", "", "", "", null));
            loadStatus.execute();
        } else {
            Toast.makeText(this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        if (helper.isNetworkAvailable()) {
            LoadProfile loadProfile = new LoadProfile(new ProfileListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String isApiSuccess, String message, String user_id, String user_name, String email, String mobile, String gender, String profile) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (isApiSuccess.equals("1")) {
                            sharedPref.setUserName(user_name);
                            sharedPref.setEmail(email);
                            sharedPref.setUserMobile(mobile);
                            sharedPref.setProfileImages(profile);
                            setVariables();
                        } else {
                            helper.logout(ProfileActivity.this, sharedPref);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            },helper.callAPI(Callback.METHOD_PROFILE, 0, "", "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", null));
            loadProfile.execute();
        } else {
            Toast.makeText(ProfileActivity.this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    public void setVariables() {
        textView_name.setText(sharedPref.getUserName());
        textView_mobile.setText(sharedPref.getUserMobile());
        textView_email.setText(sharedPref.getEmail());

        if (!sharedPref.getUserMobile().trim().isEmpty()) {
            ll_mobile.setVisibility(View.VISIBLE);
            view_phone.setVisibility(View.VISIBLE);
        } else {
            ll_mobile.setVisibility(View.GONE);
            view_phone.setVisibility(View.GONE);
        }

        if (!sharedPref.getProfileImages().isEmpty()){
            try {
                Picasso.get()
                        .load(sharedPref.getProfileImages())
                        .placeholder(R.drawable.user_photo)
                        .into(iv_profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        if (Callback.isProfileUpdate) {
            Callback.isProfileUpdate = false;
            setVariables();
        }
        super.onResume();
    }

    private void setLayoutPar(Toolbar toolbar) {
        try {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = 55;
            toolbar.setLayoutParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}