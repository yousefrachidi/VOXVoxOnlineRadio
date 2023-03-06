package com.youra.radiofr.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import com.youra.radiofr.item.ItemRadio;

public class DBHelper extends SQLiteOpenHelper {

    private final EncryptData encryptData;
    private static final String DB_NAME = "database_radio.db";
    private final SQLiteDatabase db;

    private static final String TAG_ID = "id";

    private static final String TAG_RADIO_ID = "radio_id";
    private static final String TAG_CAT_ID = "cat_id";
    private static final String TAG_RADIO_TITLE = "radio_title";
    private static final String TAG_RADIO_URL = "radio_url";
    private static final String TAG_IMAGE = "image";
    private static final String TAG_IMAGE_THUMB = "image_thumb";
    private static final String TAG_AVG_RATE = "avg_rate";
    private static final String TAG_TOTAL_RATE = "total_rate";
    private static final String TAG_TOTAL_VIEWS = "total_views";
    private static final String TAG_CAT_NAME = "category_name";
    private static final String TAG_PREMIUM = "is_premium";

    private static final String TABLE_RECENT = "recent";
    private static final String TABLE_RECENT_EPISODE = "episode";

    private final String[] columns_radio = new String[]{
            TAG_ID, TAG_RADIO_ID, TAG_CAT_ID, TAG_RADIO_TITLE, TAG_RADIO_URL, TAG_IMAGE,
            TAG_IMAGE_THUMB, TAG_TOTAL_RATE, TAG_AVG_RATE, TAG_TOTAL_VIEWS, TAG_CAT_NAME,
            TAG_PREMIUM};

    // Creating table query
    private static final String CREATE_TABLE_RECENT = "create table " + TABLE_RECENT + "(" +
            TAG_ID + " integer PRIMARY KEY AUTOINCREMENT," +
            TAG_RADIO_ID + " TEXT," +
            TAG_CAT_ID + " TEXT," +
            TAG_RADIO_TITLE + " TEXT," +
            TAG_RADIO_URL + " TEXT," +
            TAG_IMAGE + " TEXT," +
            TAG_IMAGE_THUMB + " TEXT," +
            TAG_AVG_RATE + " TEXT," +
            TAG_TOTAL_RATE + " TEXT," +
            TAG_TOTAL_VIEWS + " TEXT," +
            TAG_CAT_NAME + " TEXT," +
            TAG_PREMIUM + " TEXT);";

    // Creating table query
    private static final String CREATE_TABLE_RECENT_EPISODE = "create table " + TABLE_RECENT_EPISODE + "(" +
            TAG_ID + " integer PRIMARY KEY AUTOINCREMENT," +
            TAG_RADIO_ID + " TEXT," +
            TAG_CAT_ID + " TEXT," +
            TAG_RADIO_TITLE + " TEXT," +
            TAG_RADIO_URL + " TEXT," +
            TAG_IMAGE + " TEXT," +
            TAG_IMAGE_THUMB + " TEXT," +
            TAG_AVG_RATE + " TEXT," +
            TAG_TOTAL_RATE + " TEXT," +
            TAG_TOTAL_VIEWS + " TEXT," +
            TAG_CAT_NAME + " TEXT," +
            TAG_PREMIUM + " TEXT);";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 5);
        encryptData = new EncryptData(context);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_RECENT);
            db.execSQL(CREATE_TABLE_RECENT_EPISODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    public void addToRecent(ItemRadio itemRadio) {
        Cursor cursor_delete = db.query(TABLE_RECENT, columns_radio, null, null, null, null, null);
        if (cursor_delete != null && cursor_delete.getCount() > 20) {
            cursor_delete.moveToFirst();
            db.delete(TABLE_RECENT, TAG_RADIO_ID + "=" + cursor_delete.getString(cursor_delete.getColumnIndex(TAG_RADIO_ID)), null);
        }
        if (cursor_delete != null){
            cursor_delete.close();
        }

        if (checkRecent(itemRadio.getId())) {
            db.delete(TABLE_RECENT, TAG_RADIO_ID + "=" + itemRadio.getId(), null);
        }

        String name = itemRadio.getRadioTitle().replace("'", "%27");
        String cat_name = itemRadio.getCategoryName().replace("'", "%27");
        String imageBig = encryptData.encrypt(itemRadio.getImage().replace(" ", "%20"));
        String imageSmall = encryptData.encrypt(itemRadio.getImageThumb().replace(" ", "%20"));

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_RADIO_ID, itemRadio.getId());
        contentValues.put(TAG_CAT_ID, itemRadio.getCatID());
        contentValues.put(TAG_RADIO_TITLE, name);
        contentValues.put(TAG_RADIO_URL, itemRadio.getRadioUrl());
        contentValues.put(TAG_IMAGE, imageBig);
        contentValues.put(TAG_IMAGE_THUMB, imageSmall);
        contentValues.put(TAG_AVG_RATE, itemRadio.getAverageRating());
        contentValues.put(TAG_TOTAL_RATE, itemRadio.getTotalRate());
        contentValues.put(TAG_TOTAL_VIEWS, itemRadio.getTotalViews());
        contentValues.put(TAG_CAT_NAME, cat_name);
        contentValues.put(TAG_PREMIUM, itemRadio.IsPremium());

        db.insert(TABLE_RECENT, null, contentValues);
    }

    private Boolean checkRecent(String id) {
        Cursor cursor = db.query(TABLE_RECENT, columns_radio, TAG_RADIO_ID + "=" + id, null, null, null, null);
        Boolean isFav = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return isFav;
    }

    @SuppressLint("Range")
    public ArrayList<ItemRadio> loadDataRecent(String limit) {
        ArrayList<ItemRadio> arrayList = new ArrayList<>();
        try {
            Cursor cursor = db.query(TABLE_RECENT, columns_radio, null, null, null, null, TAG_ID + " DESC", limit);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {

                    String imageBig = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE)));
                    String imageSmall = encryptData.decrypt(cursor.getString(cursor.getColumnIndex(TAG_IMAGE_THUMB)));

                    String id = cursor.getString(cursor.getColumnIndex(TAG_RADIO_ID));
                    String cat_id = cursor.getString(cursor.getColumnIndex(TAG_CAT_ID));
                    String radio_title = cursor.getString(cursor.getColumnIndex(TAG_RADIO_TITLE)).replace("%27", "'");
                    String radio_url = cursor.getString(cursor.getColumnIndex(TAG_RADIO_URL));
                    String averageRating = cursor.getString(cursor.getColumnIndex(TAG_AVG_RATE));
                    String totalRate = cursor.getString(cursor.getColumnIndex(TAG_TOTAL_RATE));
                    String category_name = cursor.getString(cursor.getColumnIndex(TAG_CAT_NAME)).replace("%27", "'");
                    String total_views = cursor.getString(cursor.getColumnIndex(TAG_TOTAL_VIEWS));
                    Boolean isPremium = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(TAG_PREMIUM)));

                    ItemRadio objItem = new ItemRadio(id, cat_id, radio_title, radio_url, imageBig, imageSmall,
                            averageRating, totalRate, total_views, category_name, isPremium,false);
                    arrayList.add(objItem);

                    cursor.moveToNext();
                }
            }
            if (cursor != null){
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    @SuppressLint("Range")
    public String getRecentIDs(String limit) {
        String radioIDs = "";
        Cursor cursor = db.query(TABLE_RECENT, new String[]{TAG_RADIO_ID}, null, null, null, null, TAG_ID + " DESC", limit);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            radioIDs = cursor.getString(cursor.getColumnIndex(TAG_RADIO_ID));
            cursor.moveToNext();
            for (int i = 1; i < cursor.getCount(); i++) {
                radioIDs = radioIDs + "," + cursor.getString(cursor.getColumnIndex(TAG_RADIO_ID));

                cursor.moveToNext();
            }
            cursor.close();
        }
        return radioIDs;
    }

    @SuppressLint("Range")
    public void addToRecentEpisode(ItemRadio itemRadio) {
        Cursor cursor_delete = db.query(TABLE_RECENT_EPISODE, columns_radio, null, null, null, null, null);
        if (cursor_delete != null && cursor_delete.getCount() > 20) {
            cursor_delete.moveToFirst();
            db.delete(TABLE_RECENT_EPISODE, TAG_RADIO_ID + "=" + cursor_delete.getString(cursor_delete.getColumnIndex(TAG_RADIO_ID)), null);
        }
        if (cursor_delete != null){
            cursor_delete.close();
        }

        if (checkRecentEpisode(itemRadio.getId())) {
            db.delete(TABLE_RECENT_EPISODE, TAG_RADIO_ID + "=" + itemRadio.getId(), null);
        }

        String name = itemRadio.getRadioTitle().replace("'", "%27");
        String cat_name = itemRadio.getCategoryName().replace("'", "%27");
        String imageBig = encryptData.encrypt(itemRadio.getImage().replace(" ", "%20"));
        String imageSmall = encryptData.encrypt(itemRadio.getImageThumb().replace(" ", "%20"));

        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_RADIO_ID, itemRadio.getId());
        contentValues.put(TAG_CAT_ID, itemRadio.getCatID());
        contentValues.put(TAG_RADIO_TITLE, name);
        contentValues.put(TAG_RADIO_URL, itemRadio.getRadioUrl());
        contentValues.put(TAG_IMAGE, imageBig);
        contentValues.put(TAG_IMAGE_THUMB, imageSmall);
        contentValues.put(TAG_AVG_RATE, itemRadio.getAverageRating());
        contentValues.put(TAG_TOTAL_RATE, itemRadio.getTotalRate());
        contentValues.put(TAG_TOTAL_VIEWS, itemRadio.getTotalViews());
        contentValues.put(TAG_CAT_NAME, cat_name);
        contentValues.put(TAG_PREMIUM, itemRadio.IsPremium());

        db.insert(TABLE_RECENT_EPISODE, null, contentValues);
    }

    private Boolean checkRecentEpisode(String id) {
        Cursor cursor = db.query(TABLE_RECENT_EPISODE, columns_radio, TAG_RADIO_ID + "=" + id, null, null, null, null);
        Boolean isFav = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return isFav;
    }

    @SuppressLint("Range")
    public String getRecentEpisodeIDs(String limit) {
        String radioIDs = "";
        Cursor cursor = db.query(TABLE_RECENT_EPISODE, new String[]{TAG_RADIO_ID}, null, null, null, null, TAG_ID + " DESC", limit);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            radioIDs = cursor.getString(cursor.getColumnIndex(TAG_RADIO_ID));
            cursor.moveToNext();
            for (int i = 1; i < cursor.getCount(); i++) {
                radioIDs = radioIDs + "," + cursor.getString(cursor.getColumnIndex(TAG_RADIO_ID));

                cursor.moveToNext();
            }
            cursor.close();
        }
        return radioIDs;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public synchronized void close () {
        if (db != null) {
            db.close();
            super.close();
        }
    }

}