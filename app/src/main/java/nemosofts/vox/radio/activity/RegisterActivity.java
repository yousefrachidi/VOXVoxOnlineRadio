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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.nemosofts.lk.ProCompatActivity;

import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.io.IOException;

import nemosofts.vox.radio.BuildConfig;
import nemosofts.vox.radio.R;
import nemosofts.vox.radio.asyncTask.LoadRegister;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;
import nemosofts.vox.radio.interfaces.SocialLoginListener;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.Helper;
import okhttp3.RequestBody;

public class RegisterActivity extends YouraCompatActivity {

    private Helper helper;
    private EditText et_email;
    private EditText et_full_name;
    private EditText et_telephone;
    private EditText et_password;
    private EditText et_confirm_password;
    private ProgressDialog progressDialog;
    private String gender = "";
    private RoundedImageView iv_profile;
    private String imagePath = "";
    private final int PICK_IMAGE_REQUEST = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        helper = new Helper(this);

        progressDialog = new ProgressDialog(this, R.style.ThemeDialog);
        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.setCancelable(false);

        et_full_name = findViewById(R.id.et_register_full_name);
        et_email = findViewById(R.id.et_register_email);
        et_telephone = findViewById(R.id.et_register_telephone);
        et_password = findViewById(R.id.et_register_password);
        et_confirm_password = findViewById(R.id.et_register_confirm_password);
        iv_profile = findViewById(R.id.iv_profile_sign);

        findViewById(R.id.rd_male).setOnClickListener(view -> gender = "Male");
        findViewById(R.id.rd_female).setOnClickListener(view -> gender = "Female");
        findViewById(R.id.tv_login_signup).setOnClickListener(view -> finish());
        findViewById(R.id.iv_profile_sign_update).setOnClickListener(v -> {
            if (checkPer()) {
                pickImage();
            }
        });

        findViewById(R.id.btn_register).setOnClickListener(view -> {
            if (validate()) {
                loadRegister();
            }
        });

        findViewById(R.id.tv_terms).setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"terms.php");
            intent.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
            ActivityCompat.startActivity(RegisterActivity.this, intent, null);
        });

        findViewById(R.id.tv_privacy_policy).setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"privacy_policy.php");
            intent.putExtra("page_title", getResources().getString(R.string.privacy_policy));
            ActivityCompat.startActivity(RegisterActivity.this, intent, null);
        });
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_register;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), PICK_IMAGE_REQUEST);
    }

    @NonNull
    private Boolean validate() {
        if (et_email.getText().toString().trim().isEmpty()) {
            et_email.setError(getResources().getString(R.string.error_email));
            et_email.requestFocus();
            return false;
        } else if (!isEmailValid(et_email.getText().toString())) {
            et_email.setError(getString(R.string.error_invalid_email));
            et_email.requestFocus();
            return false;
        } else if (et_full_name.getText().toString().trim().isEmpty()) {
            et_full_name.setError(getResources().getString(R.string.error_name));
            et_full_name.requestFocus();
            return false;
        } else if (et_telephone.getText().toString().trim().isEmpty()) {
            et_telephone.setError(getResources().getString(R.string.error_phone));
            et_telephone.requestFocus();
            return false;
        } else if (et_password.getText().toString().isEmpty()) {
            et_password.setError(getResources().getString(R.string.error_password));
            et_password.requestFocus();
            return false;
        } else if (et_password.getText().toString().endsWith(" ")) {
            et_password.setError(getResources().getString(R.string.error_pass_end_space));
            et_password.requestFocus();
            return false;
        } else if (et_confirm_password.getText().toString().isEmpty()) {
            et_confirm_password.setError(getResources().getString(R.string.error_cpassword));
            et_confirm_password.requestFocus();
            return false;
        } else if (!et_password.getText().toString().equals(et_confirm_password.getText().toString())) {
            et_confirm_password.setError(getResources().getString(R.string.error_pass_not_match));
            et_confirm_password.requestFocus();
            return false;
        } else if (gender.isEmpty()) {
            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.error_gender), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private boolean isEmailValid(@NonNull String email) {
        return email.contains("@") && !email.contains(" ");
    }

    private void loadRegister() {
        if (helper.isNetworkAvailable()) {
            RequestBody requestBody;
            if (imagePath.equals("")){
                requestBody = helper.callAPI(Callback.METHOD_REGISTER,0,"","","","","",et_full_name.getText().toString(),et_email.getText().toString(),et_telephone.getText().toString(),gender, et_password.getText().toString(),"", Callback.LOGIN_TYPE_NORMAL, null);
            }else{
                requestBody = helper.callAPI(Callback.METHOD_REGISTER,0,"","","","","",et_full_name.getText().toString(),et_email.getText().toString(),et_telephone.getText().toString(),gender, et_password.getText().toString(),"", Callback.LOGIN_TYPE_NORMAL,new File(imagePath));
            }
            LoadRegister loadRegister = new LoadRegister(new SocialLoginListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String registerSuccess, String message, String user_id, String user_name, String email, String auth_id) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        switch (registerSuccess) {
                            case "1":
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("from", "");
                                startActivity(intent);
                                finish();
                                break;
                            case "-1":
                                helper.getVerifyDialog(getString(R.string.error_unauthorized_access), message);
                                break;
                            default:
                                if (message.contains("already") || message.contains("Invalid email format")) {
                                    et_email.setError(message);
                                    et_email.requestFocus();
                                } else {
                                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            },requestBody);
            loadRegister.execute();
        } else {
            Toast.makeText(this, getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private Boolean checkPer() {
        if (Build.VERSION.SDK_INT >= 33){
            if ((ContextCompat.checkSelfPermission(RegisterActivity.this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{READ_MEDIA_IMAGES}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
            }
        } else if (Build.VERSION.SDK_INT >= 29) {
            if ((ContextCompat.checkSelfPermission(RegisterActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                return false;
            } else {
                return true;
            }
        } else {
            if ((ContextCompat.checkSelfPermission(RegisterActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
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
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.error_cannot_use_features), Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            imagePath = helper.getPathImage(uri);
            try {
                Bitmap bitmap_upload = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                iv_profile.setImageBitmap(bitmap_upload);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}