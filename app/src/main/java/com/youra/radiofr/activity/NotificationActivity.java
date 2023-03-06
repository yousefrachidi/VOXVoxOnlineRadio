package com.youra.radiofr.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.youra.radiofr.R;
import com.youra.radiofr.adapter.AdapterNotification;
import com.youra.radiofr.asyncTask.LoadNotification;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.ifSupported.IsRTL;
import com.youra.radiofr.ifSupported.IsScreenshot;
import com.youra.radiofr.interfaces.NotificationListener;
import com.youra.radiofr.item.ItemNotification;
import com.youra.radiofr.utils.ApplicationUtil;
import com.youra.radiofr.utils.EndlessRecyclerViewScrollListener;
import com.youra.radiofr.utils.Helper;
import com.youra.radiofr.utils.SharedPref;

public class NotificationActivity extends YouraCompatActivity {

    private Helper helper;
    private SharedPref sharedPref;
    private RecyclerView rv;
    private AdapterNotification adapter;
    private ArrayList<ItemNotification> arrayList;
    private ProgressBar pb;
    private String error_msg;
    private FrameLayout frameLayout;
    private int page = 1;
    private Boolean isOver = false, isScroll = false;

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

        helper = new Helper(this);
        sharedPref = new SharedPref(this);

        arrayList = new ArrayList<>();
        error_msg = getString(R.string.no_notification);
        frameLayout = findViewById(R.id.fl_empty);
        rv = findViewById(R.id.rv);
        pb = findViewById(R.id.pb);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llm);
        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(llm) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        isScroll = true;
                        loadData();
                    }, 0);
                } else {
                    adapter.hideHeader();
                }
            }
        });

        loadData();
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_notification;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    private void loadData() {
        if (helper.isNetworkAvailable()) {
            LoadNotification loadNotification = new LoadNotification(new NotificationListener() {
                @Override
                public void onStart() {
                    if (arrayList.size() == 0) {
                        pb.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onEnd(String success, ArrayList<ItemNotification> notificationArrayList) {
                    if (success.equals("1")) {
                        if (notificationArrayList.size() == 0) {
                            isOver = true;
                            try {
                                adapter.hideHeader();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            error_msg = getString(R.string.no_notification);
                            setEmpty();
                        } else {
                            page = page + 1;
                            arrayList.addAll(notificationArrayList);
                            setAdapter();
                        }
                    } else {
                        error_msg = getString(R.string.error_server_not_connected);
                        setEmpty();
                    }
                }
            }, helper.callAPI(Callback.METHOD_NOTIFICATION, page, "", "", "", "", sharedPref.getUserId(), "", "", "", "", "", "", "", null));
            loadNotification.execute();
        } else {
            error_msg = getString(R.string.error_internet_not_connected);
            setEmpty();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdapter() {
        if (!isScroll) {
            adapter = new AdapterNotification(NotificationActivity.this, arrayList);
            rv.setAdapter(adapter);
            setEmpty();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("InflateParams")
    private void setEmpty() {
        if (arrayList.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            pb.setVisibility(View.INVISIBLE);
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(error_msg);

            myView.findViewById(R.id.btn_empty_try).setOnClickListener(v -> loadData());

            frameLayout.addView(myView);
        }
    }
}