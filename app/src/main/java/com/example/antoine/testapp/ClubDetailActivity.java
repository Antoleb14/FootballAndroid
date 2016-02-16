package com.example.antoine.testapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.antoine.testapp.classes.Fixture;
import com.example.antoine.testapp.classes.Player;
import com.example.antoine.testapp.database.DBHelper;
import com.example.antoine.testapp.database.FixturesDB;
import com.example.antoine.testapp.database.PlayersDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * An activity representing a single Club detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ClubListActivity}.
 */
public class ClubDetailActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    private String idClub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        idClub = intent.getStringExtra("idClub");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ClubDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ClubDetailFragment.ARG_ITEM_ID));
            ClubDetailFragment fragment = new ClubDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.club_detail_container, fragment)
                    .commit();
        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Chargement des informations...");
        pDialog.setCancelable(false);

        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        if(ServiceNetwork.isInternetAvailable(getApplicationContext())){
            db.delete(PlayersDB.PlayerEntry.TABLE_NAME, PlayersDB.PlayerEntry.COLUMN_NAME_CLUB_ID + "=" + idClub, null);
            db.delete(FixturesDB.FixtureEntry.TABLE_NAME, FixturesDB.FixtureEntry.COLUMN_NAME_CLUB_ID + "=" + idClub, null);
            makeJsonObjectRequestForPlayers();
            makeJsonObjectRequestForFixtures();
        } else {
            displayDataFromDatabase(idClub);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, ClubListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void displayDataFromDatabase(String idClub) {

        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String[] args = new String[]{idClub};
        Cursor cursor = db.rawQuery("SELECT * FROM players WHERE idClub=?", args);
        ArrayList<Player> listOfPlayer = new ArrayList<Player>();
        if (cursor.moveToFirst()) {
            do {
                Player p = new Player(cursor.getString(cursor.getColumnIndex("idClub")), cursor.getString(cursor.getColumnIndex("name")),
                        cursor.getString(cursor.getColumnIndex("position")), cursor.getString(cursor.getColumnIndex("number")),
                        cursor.getString(cursor.getColumnIndex("birth")));
                listOfPlayer.add(p);
            } while (cursor.moveToNext());
        }

        //Log.d("Player from database","IL y en a"+listOfPlayer.size());
        Toast.makeText(getApplicationContext(), listOfPlayer.size()+" joueurs récupérées depuis la base de données.", Toast.LENGTH_SHORT).show();
        cursor.close();

        Cursor cursorFix = db.rawQuery("SELECT * FROM fixtures WHERE idClub=? order by date desc", args);
        ArrayList<Fixture> listOfFixture = new ArrayList<Fixture>();
        if (cursorFix.moveToFirst()) {
            do {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
                        Locale.FRANCE);
                String dateMatch = cursorFix.getString(cursorFix.getColumnIndex("date"));

                Date parsedDate= new Date();
                java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                try{
                    parsedDate = sdf.parse(dateMatch);
                    sqlDate = new java.sql.Date(parsedDate.getTime());
                }catch (Exception e){
                   // Log.d("MAUVAis", "parsing");
                }

                Fixture f = new Fixture(cursorFix.getString(cursorFix.getColumnIndex("idClub")), cursorFix.getString(cursorFix.getColumnIndex("homeTeam")),
                        cursorFix.getString(cursorFix.getColumnIndex("awayTeam")), cursorFix.getString(cursorFix.getColumnIndex("goalsHomeTeam")),
                        cursorFix.getString(cursorFix.getColumnIndex("goalsAwayTeam")), sqlDate);
                listOfFixture.add(f);
            } while (cursorFix.moveToNext());
            //Log.d("fixture test sans bdd", listOfFixture.get(0).getHomeTeam());
        }
        Toast.makeText(getApplicationContext(), listOfFixture.size()+" fixtures récupérées depuis la base de données.", Toast.LENGTH_SHORT).show();
        //Log.d("Fixture form database","-> IL y en a "+listOfFixture.size());
        cursorFix.close();
    }

    /**
     * Method to make json object request where json response starts wtih {
     */
    private void makeJsonObjectRequestForPlayers() {

        showpDialog();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                "http://api.football-data.org/v1/teams/"+idClub+"/players", null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                DBHelper mDbHelper = new DBHelper(getApplicationContext());
                // Gets the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                // Create a new map of values, where column names are the keys

                try {
                    // Parsing json object response
                    // response will be a json object

                    //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                    JSONArray array = response.getJSONArray("players");



                    ArrayList<Player> listOfPlayer = new ArrayList<Player>();
                    for(int i = 0; i < array.length(); i++){
                        ContentValues values = new ContentValues();
                        values.put(PlayersDB.PlayerEntry.COLUMN_NAME_CLUB_ID, idClub);
                        values.put(PlayersDB.PlayerEntry.COLUMN_NAME_NAME, array.getJSONObject(i).getString("name"));
                        values.put(PlayersDB.PlayerEntry.COLUMN_NAME_POSITION, array.getJSONObject(i).getString("position"));
                        values.put(PlayersDB.PlayerEntry.COLUMN_NAME_NUMBER, array.getJSONObject(i).getString("jerseyNumber"));
                        values.put(PlayersDB.PlayerEntry.COLUMN_NAME_BIRTH, array.getJSONObject(i).getString("dateOfBirth"));
                        // Insert the new row, returning the primary key value of the new row
                        long newRowId;

                        newRowId = db.insert(
                                PlayersDB.PlayerEntry.TABLE_NAME,
                                null,
                                values);


                        Player p = new Player(idClub, array.getJSONObject(i).getString("name"),
                            array.getJSONObject(i).getString("position"), array.getJSONObject(i).getString("jerseyNumber"),
                                array.getJSONObject(i).getString("dateOfBirth"));
                        listOfPlayer.add(p);

                    }

                   // Log.d("Player from internet", "-> il y en a " + listOfPlayer.size());
                    Toast.makeText(getApplicationContext(), listOfPlayer.size()+" joueurs récupérées depuis l'API.", Toast.LENGTH_SHORT).show();


                } catch (JSONException e) {
                    e.printStackTrace();
                    /*Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();*/
                } finally {
                    //Log.d("INSERTIONOK", "OK");
                    db.close();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                hidepDialog();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("X-Auth-Token", "9e18efd7cace4eaca5c7ff0542be1888");
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    /**
     * Method to make json object request where json response starts wtih {
     */
    private void makeJsonObjectRequestForFixtures() {

        showpDialog();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                "http://api.football-data.org/v1/teams/"+idClub+"/fixtures", null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, response.toString());

                DBHelper mDbHelper = new DBHelper(getApplicationContext());
                // Gets the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                // Create a new map of values, where column names are the keys


                try {
                    // Parsing json object response
                    // response will be a json object

                    //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                    JSONArray array = response.getJSONArray("fixtures");

                    ArrayList<Fixture> listOfFixture = new ArrayList<Fixture>();
                    for(int i = 0; i < array.length(); i++){

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                                Locale.FRANCE);
                        String dateMatch = array.getJSONObject(i).getString("date");

                        Date parsedDate= new Date();
                        java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
                        try{
                            parsedDate = sdf.parse(dateMatch);
                            sqlDate = new java.sql.Date(parsedDate.getTime());
                        }catch (Exception e){
                            //Log.d("MAUVAis", "parsing");
                        }

                        String stringDate = ""+sqlDate;

                        ContentValues values = new ContentValues();
                        values.put(FixturesDB.FixtureEntry.COLUMN_NAME_CLUB_ID, idClub);
                        values.put(FixturesDB.FixtureEntry.COLUMN_NAME_HOME_TEAM, array.getJSONObject(i).getString("homeTeamName"));
                        values.put(FixturesDB.FixtureEntry.COLUMN_NAME_AWAY_TEAM, array.getJSONObject(i).getString("awayTeamName"));

                        String goalsHomeTeam="0";
                        if (array.getJSONObject(i).getJSONObject("result").optString("goalsHomeTeam")==null) {
                            goalsHomeTeam="0";
                        } else {
                            goalsHomeTeam=array.getJSONObject(i).getJSONObject("result").optString("goalsHomeTeam");
                        }

                        String goalsAwayTeam="0";
                        if (array.getJSONObject(i).getJSONObject("result").optString("goalsAwayTeam")==null) {
                            goalsAwayTeam="0";
                        } else {
                            goalsAwayTeam=array.getJSONObject(i).getJSONObject("result").optString("goalsAwayTeam");
                        }

                        values.put(FixturesDB.FixtureEntry.COLUMN_NAME_GOALS_HOME_TEAM, goalsHomeTeam);
                        values.put(FixturesDB.FixtureEntry.COLUMN_NAME_GOALS_AWAY_TEAM, goalsAwayTeam);
                        values.put(FixturesDB.FixtureEntry.COLUMN_NAME_DATE, stringDate);
                        // Insert the new row, returning the primary key value of the new row
                        long newRowId;

                        newRowId = db.insert(
                                FixturesDB.FixtureEntry.TABLE_NAME,
                                null,
                                values);


                        Fixture f = new Fixture(idClub, array.getJSONObject(i).getString("homeTeamName"),
                                array.getJSONObject(i).getString("awayTeamName"), goalsHomeTeam,
                                goalsAwayTeam, sqlDate);
                        listOfFixture.add(f);


                    }
                    //Log.d("Fixture form internet","-> il y en a "+listOfFixture.size());
                    Toast.makeText(getApplicationContext(), listOfFixture.size()+" fixtures récupérées depuis l'API.", Toast.LENGTH_SHORT).show();

                    // Log.d("fixture test avec bdd", listOfFixture.get(0).getHomeTeam());

                } catch (JSONException e) {
                    e.printStackTrace();
                    /*Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();*/
                }finally {
                    db.close();
                }
                hidepDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                hidepDialog();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("X-Auth-Token", "9e18efd7cace4eaca5c7ff0542be1888");
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
