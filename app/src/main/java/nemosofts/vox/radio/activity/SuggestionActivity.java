package nemosofts.vox.radio.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.nemosofts.lk.ProCompatActivity;

import java.io.File;
import java.io.IOException;

import nemosofts.vox.radio.R;
import nemosofts.vox.radio.asyncTask.LoadStatus;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;
import nemosofts.vox.radio.interfaces.SuccessListener;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.Helper;
import nemosofts.vox.radio.utils.SharedPref;

public class SuggestionActivity extends YouraCompatActivity {

    private Helper helper;
    private SharedPref sharedPref;
    private TextView editText_title, editText_desc;
    private ImageView iv_sugg;
    private String imagePath = "";
    private Bitmap bitmap_upload;
    private ProgressDialog progressDialog;
    private final int PICK_IMAGE_REQUEST = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        helper = new Helper(this);
        sharedPref = new SharedPref(this);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        }

        progressDialog = new ProgressDialog(SuggestionActivity.this, R.style.ThemeDialog);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        iv_sugg = findViewById(R.id.iv_sugg);
        editText_desc = findViewById(R.id.et_description);
        editText_title = findViewById(R.id.et_title);

        findViewById(R.id.ll_sugg).setOnClickListener(v -> {
            if (checkPer()) {
                pickImage();
            }
        });

        findViewById(R.id.button_sugg_submit).setOnClickListener(v -> {
            if(editText_title.getText().toString().equals("")) {
                Toast.makeText(SuggestionActivity.this, getString(R.string.enter_your_title_here_suggestion), Toast.LENGTH_SHORT).show();
            } else if(editText_desc.getText().toString().equals("")) {
                Toast.makeText(SuggestionActivity.this, getString(R.string.enter_your_description_here_suggestion), Toast.LENGTH_SHORT).show();
            } else if(imagePath!= null && imagePath.equals("")) {
                Toast.makeText(SuggestionActivity.this, getString(R.string.select_image), Toast.LENGTH_SHORT).show();
            } else {
                if(sharedPref.isLogged()) {
                    loadSuggestion();
                } else {
                    helper.clickLogin();
                }
            }
        });

        LinearLayout adView = findViewById(R.id.ll_adView);
        helper.showBannerAd(adView);
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_suggestion;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    public void loadSuggestion() {
        if (helper.isNetworkAvailable()) {
            LoadStatus loadSuggestion = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        imagePath = "";
                        bitmap_upload = null;
                        editText_title.setText("");
                        editText_desc.setText("");
                        iv_sugg.setImageDrawable(getResources().getDrawable(R.drawable.logo));
                        uploadDialog(message);
                    }else {
                        Toast.makeText(SuggestionActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.callAPI(Callback.METHOD_SUGGESTION, 0, "", "", editText_title.getText().toString(),editText_desc.getText().toString(), sharedPref.getUserId(), "", "", "", "", "", "", "", new File(imagePath)));
            loadSuggestion.execute();
        } else {
            Toast.makeText(this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }

    private void uploadDialog(String message) {
        AlertDialog.Builder alertDialog;
        alertDialog = new AlertDialog.Builder(SuggestionActivity.this, R.style.ThemeDialog);
        alertDialog.setTitle(getString(R.string.upload_success));
        alertDialog.setMessage(message);
        alertDialog.setNegativeButton(getString(R.string.ok), (dialog, which) -> {
        });
        alertDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            imagePath = helper.getPathImage(uri);
            try {
                bitmap_upload = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                iv_sugg.setImageBitmap(bitmap_upload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private Boolean checkPer() {
        if (Build.VERSION.SDK_INT >= 33){
            if ((ContextCompat.checkSelfPermission(SuggestionActivity.this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{READ_MEDIA_IMAGES}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
            }
        } else if (Build.VERSION.SDK_INT >= 29) {
            if ((ContextCompat.checkSelfPermission(SuggestionActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
            }
        } else {
            if ((ContextCompat.checkSelfPermission(SuggestionActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean canUseExternalStorage = false;
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                canUseExternalStorage = true;
            }
            if (!canUseExternalStorage) {
                Toast.makeText(SuggestionActivity.this, getResources().getString(R.string.error_cannot_use_features), Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}