package com.epicure.chronos.epicure.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Chronos on 04/03/2017.
 */

public class FavoriteDbHelper extends SQLiteOpenHelper {

    public FavoriteDbHelper(Context context) {
        super(context, FavoriteContract.DB_NAME, null,  FavoriteContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + FavoriteContract.FavoriteEntry.TABLE + " ( "
                + FavoriteContract.FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FavoriteContract.FavoriteEntry.COL_FAV_URL + " TEXT NOT NULL);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteContract.FavoriteEntry.TABLE);
        onCreate(db);
    }
}
