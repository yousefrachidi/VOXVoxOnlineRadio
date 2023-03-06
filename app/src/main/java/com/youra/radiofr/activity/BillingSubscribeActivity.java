package com.youra.radiofr.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.youra.radiofr.BuildConfig;
import com.youra.radiofr.R;
import com.youra.radiofr.adapter.PlanAdapter;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.ifSupported.IsRTL;
import com.youra.radiofr.ifSupported.IsScreenshot;
import com.youra.radiofr.item.ItemPlan;
import com.youra.radiofr.utils.ApplicationUtil;
import com.youra.radiofr.utils.Helper;
import com.youra.radiofr.utils.SharedPref;
import okhttp3.RequestBody;

public class BillingSubscribeActivity extends YouraCompatActivity {

    private Helper helper;
    private SharedPref sharedPref;
    private ProgressBar pb;
    private RecyclerView rv;
    private TextView proceed;
    private String error_msg;
    private FrameLayout frameLayout;
    private ArrayList<ItemPlan> mListItem;
    private PlanAdapter adapter;
    private int selectedPlan = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        helper = new Helper(this);
        sharedPref = new SharedPref(this);

        mListItem = new ArrayList<>();
        pb = findViewById(R.id.pb);
        frameLayout = findViewById(R.id.fl_empty);
        proceed = findViewById(R.id.tv_btn_proceed);

        rv = findViewById(R.id.rv_plan);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(BillingSubscribeActivity.this, LinearLayoutManager.VERTICAL, false));
        rv.setFocusable(false);
        rv.setNestedScrollingEnabled(false);

        findViewById(R.id.tv_terms).setOnClickListener(view -> {
            Intent intent = new Intent(BillingSubscribeActivity.this, WebActivity.class);
            intent.putExtra("web_url", BuildConfig.BASE_URL+"terms.php");
            intent.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
            ActivityCompat.startActivity(BillingSubscribeActivity.this, intent, null);
        });

        getPlan();
    }

    private void getPlan() {
        if (helper.isNetworkAvailable()) {
            LoadPlan loadPlan = new LoadPlan(new PlanListener() {
                @Override
                public void onStart() {
                    pb.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.GONE);
                }

                @Override
                public void onEnd(String success, boolean verifyStatus, String message, ArrayList<ItemPlan> itemPlans) {
                    if (success.equals("1")) {
                        if (verifyStatus) {
                            rv.setVisibility(View.VISIBLE);
                            pb.setVisibility(View.GONE);
                            mListItem.addAll(itemPlans);
                            displayData();
                        }else {
                            helper.getVerifyDialog(getString(R.string.error_unauthorized_access), message);
                        }
                    }else {
                        error_msg = getString(R.string.error_server_not_connected);
                        setEmpty();
                    }
                }
            }, helper.callAPI(Callback.METHOD_PLAN, 0, "", "", "", "", "", "", "", "", "", "", "", "", null));
            loadPlan.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayData() {
        adapter = new PlanAdapter(BillingSubscribeActivity.this, mListItem);
        rv.setAdapter(adapter);
        adapter.select(-1);

        adapter.setOnItemClickListener(position -> {
            selectedPlan = position;
            adapter.select(position);
            proceed.setText("Try for "+ mListItem.get(position).getPlanPrice()+" "+mListItem.get(position).getPlanCurrencyCode());
        });

        proceed.setOnClickListener(view -> {
            if(sharedPref.isLogged()) {
                if (!Callback.isPurchases){
                    if (selectedPlan != -1){
                        ItemPlan itemPlan = mListItem.get(selectedPlan);
                        Intent intent = new Intent(BillingSubscribeActivity.this, BillingConnectorActivity.class);
                        intent.putExtra("planId", itemPlan.getPlanId());
                        intent.putExtra("planName", itemPlan.getPlanName());
                        intent.putExtra("planPrice", itemPlan.getPlanPrice());
                        intent.putExtra("planDuration", itemPlan.getPlanDuration());
                        intent.putExtra("planCurrencyCode", itemPlan.getPlanCurrencyCode());
                        intent.putExtra("subscription_id", itemPlan.getSubscription_id());
                        intent.putExtra("base_key", itemPlan.getBase_key());
                        startActivity(intent);
                    }else {
                        proceed.setText("no selected");
                    }
                }else {
                    Toast.makeText(BillingSubscribeActivity.this, "Item already subscribed", Toast.LENGTH_SHORT).show();
                }
            }else {
                helper.clickLogin();
            }
        });
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_billing_subscribe;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    private static class LoadPlan extends AsyncTask<String, String, String> {

        private final RequestBody requestBody;
        private final PlanListener planListener;
        private final ArrayList<ItemPlan> arrayList = new ArrayList<>();
        private String message = "";
        private boolean verifyStatus = true;

        public LoadPlan(PlanListener planListener, RequestBody requestBody) {
            this.planListener = planListener;
            this.requestBody = requestBody;
        }

        @Override
        protected void onPreExecute() {
            planListener.onStart();
            super.onPreExecute();
        }

        @NonNull
        @Override
        protected String doInBackground(String... strings) {
            String json = ApplicationUtil.responsePost(Callback.API_URL, requestBody);
            try {
                JSONObject jOb = new JSONObject(json);
                JSONArray jsonArray = jOb.getJSONArray(Callback.TAG_ROOT);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject objJson = jsonArray.getJSONObject(i);

                    if (!objJson.has(Callback.TAG_SUCCESS)) {

                        String id = objJson.getString("id");
                        String plan_name = objJson.getString("plan_name");
                        String plan_duration = objJson.getString("plan_duration");
                        String plan_price = objJson.getString("plan_price");
                        String currency_code = objJson.getString("currency_code");
                        String subscription_id = objJson.getString("subscription_id");
                        String base_key = objJson.getString("base_key");

                        ItemPlan item = new ItemPlan(id, plan_name, plan_duration, plan_price, currency_code, subscription_id, base_key);
                        arrayList.add(item);
                    } else {
                        verifyStatus = objJson.getBoolean(Callback.TAG_SUCCESS);
                        message = objJson.getString(Callback.TAG_MSG);
                    }
                }
                return "1";
            } catch (Exception ee) {
                ee.printStackTrace();
                return "0";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            planListener.onEnd(s, verifyStatus, message, arrayList);
            super.onPostExecute(s);
        }
    }

    @SuppressLint("InflateParams")
    private void setEmpty() {
        pb.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

        frameLayout.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View myView = inflater.inflate(R.layout.layout_empty, null);

        TextView textView = myView.findViewById(R.id.tv_empty_msg);
        textView.setText(error_msg);

        myView.findViewById(R.id.btn_empty_try).setOnClickListener(v -> getPlan());

        frameLayout.addView(myView);
    }

    private interface PlanListener {
        void onStart();
        void onEnd(String success, boolean verifyStatus, String message, ArrayList<ItemPlan> itemPlans);
    }
}