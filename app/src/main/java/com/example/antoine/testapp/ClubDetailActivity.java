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
import com.example.antoine.testapp.dummy.Club;
import com.example.antoine.testapp.dummy.LeagueClubs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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
            Log.d("TEST", "INTERNET");
            db.delete(PlayersDB.PlayerEntry.TABLE_NAME, ClubDB.ClubEntry.COLUMN_NAME_CLUB_ID + "=" + idClub, null);
            makeJsonObjectRequestForPlayers();
            makeJsonObjectRequestForFixtures();
        } else {
            Log.d("TEST", "PAS INTERNET");
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
        cursor.close();
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

                } catch (JSONException e) {
                    e.printStackTrace();
                    /*Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();*/
                } finally {
                    Log.d("INSERTIONOK", "OK");
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
                "http://api.football-data.org/v1/teams/"+idClub+"/players", null, new Response.Listener<JSONObject>() {

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
                    JSONArray array = response.getJSONArray("players");


                    for(int i = 0; i < array.length(); i++){

                        Log.d("Joueur", array.getJSONObject(i).getString("name"));


                    }

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
