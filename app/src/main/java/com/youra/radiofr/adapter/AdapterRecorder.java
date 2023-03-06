package com.youra.radiofr.adapter;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.youra.radiofr.R;
import com.youra.radiofr.callback.Callback;
import com.youra.radiofr.item.ItemRecorder;
import com.youra.radiofr.utils.ApplicationUtil;
import com.youra.radiofr.utils.RecorderPlayer;

public class AdapterRecorder extends RecyclerView.Adapter<AdapterRecorder.MyViewHolder> {

    private final Context context;
    private final ArrayList<ItemRecorder> arrayList;
    private final RecyclerItemClickListener listener;
    private final RecorderPlayer recorderPlayer;

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_title;
        private final TextView tv_artist;
        private final TextView views;
        private final ImageView play;
        private final ImageView pause;
        private final ImageView option;

        MyViewHolder(View view) {
            super(view);
            tv_title = itemView.findViewById(R.id.tv_songlist_name);
            tv_artist =  itemView.findViewById(R.id.tv_songlist_cat);
            play = itemView.findViewById(R.id.play);
            pause = itemView.findViewById(R.id.pause);
            views = itemView.findViewById(R.id.tv_songlist_vie);
            option = itemView.findViewById(R.id.iv_option);
        }
    }

    public AdapterRecorder(Context context, ArrayList<ItemRecorder> arrayList, RecyclerItemClickListener listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener = listener;
        recorderPlayer = new RecorderPlayer(context);
        recorderPlayer.onCreate();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_recorder, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final ItemRecorder item = arrayList.get(position);

        holder.tv_title.setText(getTitleData(item.getTitle()));
        holder.tv_artist.setText(item.getDuration());
        holder.views.setText(getStringSizeLengthFile(item.getFilesets()));

        final String finalUrl =  item.getMp3();

        RecorderPlayer.exoPlayerRecorder.addListener(new Player.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Player.Listener.super.onPlayerStateChanged(playWhenReady, playbackState);
                if (playbackState == Player.STATE_ENDED) {
                    holder.play.setVisibility(View.VISIBLE);
                    holder.pause.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                RecorderPlayer.exoPlayerRecorder.setPlayWhenReady(false);
            }
        });

        holder.pause.setOnClickListener(view -> {
            if (RecorderPlayer.exoPlayerRecorder.getPlayWhenReady()) {
                RecorderPlayer.exoPlayerRecorder.setPlayWhenReady(false);
                holder.pause.setImageResource(R.drawable.ic_play);
            } else {
                RecorderPlayer.exoPlayerRecorder.setPlayWhenReady(true);
                holder.pause.setImageResource(R.drawable.ic_pause);
            }
        });

        holder.play.setOnClickListener(view -> {
            listener.onClickListener(item, position);
            recorderPlayer.setUrl(finalUrl);
            holder.pause.setImageResource(R.drawable.ic_pause);
            holder.play.setImageResource(R.drawable.ic_play);
            holder.play.setVisibility(View.GONE);
            holder.pause.setVisibility(View.VISIBLE);

        });

        if (RecorderPlayer.getIsPlaying() && Callback.playPos_rc <= holder.getAdapterPosition() && Callback.arrayList_play_rc.get(Callback.playPos_rc).getId().equals(arrayList.get(position).getId())) {
            holder.play.setVisibility(View.GONE);
            holder.pause.setVisibility(View.VISIBLE);
        } else {
            holder.pause.setVisibility(View.GONE);
            holder.play.setVisibility(View.VISIBLE);
        }

        holder.option.setOnClickListener(view -> openOptionPopUp(holder.option, holder.getAdapterPosition(), item));
    }

    @NonNull
    public static String getStringSizeLengthFile(long size) {
        DecimalFormat df = new DecimalFormat("0.00");
        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;
        if(size < sizeMb)
            return df.format(size / sizeKb)+ " Kb";
        else if(size < sizeGb)
            return df.format(size / sizeMb) + " Mb";
        else if(size < sizeTerra)
            return df.format(size / sizeGb) + " Gb";
        return "";
    }

    private void openOptionPopUp(ImageView iv_option, final int pos, ItemRecorder itemRecorder) {
        ContextThemeWrapper ctw;
        if (ApplicationUtil.isDarkMode()) {
            ctw = new ContextThemeWrapper(context, R.style.PopupMenuDark);
        } else {
            ctw = new ContextThemeWrapper(context, R.style.PopupMenuLight);
        }
        PopupMenu popup = new PopupMenu(ctw, iv_option);
        popup.getMenuInflater().inflate(R.menu.popup_song_off, popup.getMenu());
        popup.setForceShowIcon(true);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.popup_delete) {
                openDeleteDialog(pos);
            }else if (item.getItemId() == R.id.popup_share) {
                try {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("audio/mp3");
                    share.putExtra(Intent.EXTRA_STREAM, Uri.parse(itemRecorder.getMp3()));
                    share.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.listening) + " - " + getTitleData(itemRecorder.getTitle()) + "\n\nvia " + context.getResources().getString(R.string.app_name) + " - http://play.google.com/store/apps/details?id=" + context.getPackageName());
                    context.startActivity(Intent.createChooser(share, context.getResources().getString(R.string.share_record)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        });
        popup.show();
    }

    @NonNull
    public static String getTitleData(@NonNull String title) {
        String titlePath;
        titlePath = title.replace(".mp3", "");
        return titlePath;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void openDeleteDialog(final int pos) {
        final File file = new File(arrayList.get(pos).getMp3());
        AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.ThemeDialog);

        dialog.setTitle(context.getString(R.string.delete));
        dialog.setMessage(context.getString(R.string.sure_delete_recording));
        dialog.setPositiveButton(context.getString(R.string.delete), (dialogInterface, i) -> {
            final String where = MediaStore.MediaColumns.DATA + "=?";
            final String[] selectionArgs = new String[] {
                    file.getAbsolutePath()
            };
            final ContentResolver contentResolver = context.getContentResolver();
            final Uri filesUri = MediaStore.Files.getContentUri("external");
            try {
                contentResolver.delete(filesUri, where, selectionArgs);
                file.delete();
                notifyDataSetChanged();
                arrayList.remove(pos);
                notifyItemRemoved(pos);
                Toast.makeText(context, context.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        dialog.setNegativeButton(context.getString(R.string.cancel), (dialogInterface, i) -> {

        });
        dialog.show();
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface RecyclerItemClickListener{
        void onClickListener(ItemRecorder itemData, int position);
    }

}