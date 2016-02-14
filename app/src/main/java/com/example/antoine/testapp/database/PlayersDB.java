package com.example.antoine.testapp.database;

import android.provider.BaseColumns;

/**
 * Created by guillaumebrosse on 14/02/16.
 */
public final class PlayersDB {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PlayersDB() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlayerEntry.TABLE_NAME + " (" +
                    PlayerEntry._ID + " INTEGER PRIMARY KEY," +
                    PlayerEntry.COLUMN_NAME_CLUB_ID + TEXT_TYPE + COMMA_SEP +
                    PlayerEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    PlayerEntry.COLUMN_NAME_POSITION + TEXT_TYPE + COMMA_SEP +
                    PlayerEntry.COLUMN_NAME_NUMBER + TEXT_TYPE + COMMA_SEP +
                    PlayerEntry.COLUMN_NAME_BIRTH + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlayerEntry.TABLE_NAME;

    /* Inner class that defines the table contents */
    public static abstract class PlayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "players";
        public static final String COLUMN_NAME_CLUB_ID = "idClub";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_NUMBER = "number";
        public static final String COLUMN_NAME_BIRTH = "birth";
    }
}

