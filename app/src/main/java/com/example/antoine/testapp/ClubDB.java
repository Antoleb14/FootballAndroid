package com.example.antoine.testapp;

import android.provider.BaseColumns;

/**
 * Created by guillaumebrosse on 14/02/16.
 */
public final class ClubDB {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public ClubDB() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ClubEntry.TABLE_NAME + " (" +
                    ClubEntry._ID + " INTEGER PRIMARY KEY," +
                    ClubEntry.COLUMN_NAME_LEAGUE_ID + TEXT_TYPE + COMMA_SEP +
                    ClubEntry.COLUMN_NAME_CLUB_ID + TEXT_TYPE + COMMA_SEP +
                    ClubEntry.COLUMN_NAME_CLUB_FIXTURES + TEXT_TYPE + COMMA_SEP +
                    ClubEntry.COLUMN_NAME_CLUB_PLAYERS + TEXT_TYPE + COMMA_SEP +
                    ClubEntry.COLUMN_NAME_TEAM_NAME + TEXT_TYPE + COMMA_SEP +
                    ClubEntry.COLUMN_NAME_ICON_LINK + TEXT_TYPE + COMMA_SEP +
                    ClubEntry.COLUMN_NAME_RANK + TEXT_TYPE + COMMA_SEP +
                    ClubEntry.COLUMN_NAME_MARKET_VALUE + TEXT_TYPE + COMMA_SEP +
                    ClubEntry.COLUMN_NAME_LAST_MATCH + TEXT_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ClubEntry.TABLE_NAME;

    /* Inner class that defines the table contents */
    public static abstract class ClubEntry implements BaseColumns {
        public static final String TABLE_NAME = "league";
        public static final String COLUMN_NAME_LEAGUE_ID = "idLeague";
        public static final String COLUMN_NAME_CLUB_ID = "idClub";
        public static final String COLUMN_NAME_CLUB_FIXTURES = "fixturesClub";
        public static final String COLUMN_NAME_CLUB_PLAYERS = "playersClub";
        public static final String COLUMN_NAME_TEAM_NAME = "name";
        public static final String COLUMN_NAME_ICON_LINK = "iconLink";
        public static final String COLUMN_NAME_RANK = "rank";
        public static final String COLUMN_NAME_MARKET_VALUE = "marketValue";
        public static final String COLUMN_NAME_LAST_MATCH = "lastMatch";
    }
}

