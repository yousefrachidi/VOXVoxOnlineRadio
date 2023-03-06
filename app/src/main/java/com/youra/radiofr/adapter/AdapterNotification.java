package com.youra.radiofr.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.youra.radiofr.R;
import com.youra.radiofr.asyncTask.LoadStatus;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.interfaces.SuccessListener;
import com.youra.radiofr.item.ItemNotification;
import com.youra.radiofr.utils.Helper;
import com.youra.radiofr.utils.SharedPref;

public class AdapterNotification extends RecyclerView.Adapter {

    private final ArrayList<ItemNotification> arrayList;
    public final Context context;
    private final Helper helper;
    private final SharedPref sharedPref;
    private final int VIEW_ITEM = 1;

    public AdapterNotification(Context context, ArrayList<ItemNotification> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
        helper = new Helper(context);
        sharedPref = new SharedPref(context);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView_name;
        private final TextView textView_comment;
        private final TextView textView_date;
        private final RelativeLayout rl_not_close;

        MyViewHolder(View view) {
            super(view);
            textView_name = view.findViewById(R.id.tv_not_title);
            textView_comment = view.findViewById(R.id.tv_not_note);
            textView_date = view.findViewById(R.id.tv_not_date);
            rl_not_close = view.findViewById(R.id.rl_not_close);
        }
    }

    private static class ProgressViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("StaticFieldLeak")
        private static ProgressBar progressBar;

        private ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notification, parent, false);
            return new MyViewHolder(itemView);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_progressbar, parent, false);
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).textView_name.setText(arrayList.get(position).getName());
            ((MyViewHolder) holder).textView_comment.setText(arrayList.get(position).getNot());
            ((MyViewHolder) holder).textView_date.setText(arrayList.get(position).getDate());
            ((MyViewHolder) holder).textView_date.setText(arrayList.get(position).getDate());

            ((MyViewHolder) holder).rl_not_close.setOnClickListener(view -> {
                if (position <= arrayList.size()) {
                    loadRemove(holder.getAdapterPosition());
                }
            });

        } else {
            if (getItemCount() == 1) {
                ProgressViewHolder.progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void loadRemove(int pos) {
        if (helper.isNetworkAvailable()) {
            ProgressDialog progressDialog = new ProgressDialog(context, R.style.ThemeDialog);
            progressDialog.setMessage(context.getString(R.string.loading));
            LoadStatus loadStatus = new LoadStatus(new SuccessListener() {
                @Override
                public void onStart() {
                    progressDialog.show();
                }

                @Override
                public void onEnd(String success, String status, String message) {
                    progressDialog.dismiss();
                    if (success.equals("1")) {
                        if (status.equals("1")) {
                            arrayList.remove(pos);
                            notifyItemRemoved(pos);
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_server_not_connected), Toast.LENGTH_SHORT).show();
                    }
                }
            }, helper.callAPI(Callback.METHOD_REMOVE_NOTIFICATION, 0,arrayList.get(pos).getId(),"","","", sharedPref.getUserId(),"","","","","","","",null));
            loadStatus.execute();
        } else {
            Toast.makeText(context, context.getString(R.string.error_internet_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return isProgressPos(position) ? 0 : VIEW_ITEM;
    }

    private boolean isProgressPos(int position) {
        return position == arrayList.size();
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }
}