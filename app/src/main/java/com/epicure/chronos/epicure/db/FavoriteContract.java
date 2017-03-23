package com.epicure.chronos.epicure.db;

import android.provider.BaseColumns;
/**
 * Created by Chronos on 04/03/2017.
 */

public class FavoriteContract {
    public static final String DB_NAME = "com.epicure.chronos.epicure.db";
    public static final int DB_VERSION = 1;

    public class FavoriteEntry implements BaseColumns {
        public static final String TABLE = "favorite";
        public static final String COL_FAV_URL = "url";
    }
}
