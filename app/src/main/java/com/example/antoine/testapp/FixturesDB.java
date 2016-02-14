package com.example.antoine.testapp;

import android.provider.BaseColumns;

/**
 * Created by guillaumebrosse on 14/02/16.
 */
public final class FixturesDB {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FixturesDB() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String DATE_TYPE = " DATE";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FixtureEntry.TABLE_NAME + " (" +
                    FixtureEntry._ID + " INTEGER PRIMARY KEY," +
                    FixtureEntry.COLUMN_NAME_CLUB_ID + TEXT_TYPE + COMMA_SEP +
                    FixtureEntry.COLUMN_NAME_HOME_TEAM + TEXT_TYPE + COMMA_SEP +
                    FixtureEntry.COLUMN_NAME_AWAY_TEAM + TEXT_TYPE + COMMA_SEP +
                    FixtureEntry.COLUMN_NAME_GOALS_HOME_TEAM + TEXT_TYPE + COMMA_SEP +
                    FixtureEntry.COLUMN_NAME_GOALS_AWAY_TEAM + TEXT_TYPE + COMMA_SEP +
                    FixtureEntry.COLUMN_NAME_DATE + DATE_TYPE +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FixtureEntry.TABLE_NAME;

    /* Inner class that defines the table contents */
    public static abstract class FixtureEntry implements BaseColumns {
        public static final String TABLE_NAME = "fixtures";
        public static final String COLUMN_NAME_CLUB_ID = "idClub";
        public static final String COLUMN_NAME_HOME_TEAM = "homeTeam";
        public static final String COLUMN_NAME_AWAY_TEAM = "awayTeam";
        public static final String COLUMN_NAME_GOALS_HOME_TEAM = "goalsHomeTeam";
        public static final String COLUMN_NAME_GOALS_AWAY_TEAM = "goalsAwayTeam";
        public static final String COLUMN_NAME_DATE = "date";
    }

}

