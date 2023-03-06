package com.youra.radiofr.utils.purchases;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;


import com.youra.radiofr.R;
import com.youra.radiofr.asyncTask.LoadStatus;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.SuccessListener;
import com.youra.radiofr.utils.Helper;
import com.youra.radiofr.utils.SharedPref;

public class Transaction {

    private final ProgressDialog pDialog;
    private final Activity mContext;
    private final Helper helper;

    public Transaction(Activity context) {
        this.mContext = context;
        helper = new Helper(mContext);
        pDialog = new ProgressDialog(mContext, R.style.ThemeDialog);
    }

    public void purchasedItem(String planId, String planName, String planPrice, String planDuration, String planCurrencyCode) {
        if (helper.isNetworkAvailable()) {
            LoadStatus transaction = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    showProgressDialog();
                }

                @Override
                public void onEnd(String success, String status, String message) {
                    dismissProgressDialog();
                    if (success.equals("1")) {
                        if (status.equals("1")) {
                            ActivityCompat.recreate(mContext);
                        }
                        Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext,mContext.getString(R.string.error_server_not_connected),Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.callAPI(Callback.TRANSACTION_URL, 0, planId, planName, planPrice,planDuration, new SharedPref(mContext).getUserId(), planCurrencyCode, "", "", "", "", "", "", null));
            transaction.execute();
        } else {
            Toast.makeText(mContext,mContext.getString(R.string.error_internet_not_connected),Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog() {
        pDialog.setMessage(mContext.getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog.isShowing()){
            pDialog.dismiss();
        }
    }

}
