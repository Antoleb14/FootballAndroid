package com.example.antoine.testapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.antoine.testapp.classes.ClubClass;
import com.example.antoine.testapp.classes.Fixture;
import com.example.antoine.testapp.classes.League;
import com.example.antoine.testapp.classes.Player;
import com.example.antoine.testapp.database.DBHelper;
import com.example.antoine.testapp.database.FixturesDB;
import com.example.antoine.testapp.database.PlayersDB;
import com.example.antoine.testapp.dummy.Club;
import com.example.antoine.testapp.dummy.DummyContent;
import com.example.antoine.testapp.dummy.LeagueClubs;

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
 * A fragment representing a single Club detail screen.
 * This fragment is either contained in a {@link ClubListActivity}
 * in two-pane mode (on tablets) or a {@link ClubDetailActivity}
 * on handsets.
 */
public class ClubDetailFragment extends Fragment {
    private ProgressDialog pDialog;
    private String idClub;
    private ClubClass club;
    private ArrayList<Player> listPlayers;
    private ArrayList<Fixture> listFixtures,
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Club mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ClubDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = LeagueClubs.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            idClub=mItem.idApi;

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.nom);
            }
        }

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Chargement des informations...");
        pDialog.setCancelable(false);

        DBHelper mDbHelper = new DBHelper(getActivity().getApplicationContext());
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        if(ServiceNetwork.isInternetAvailable(getActivity().getApplicationContext())){
            db.delete(PlayersDB.PlayerEntry.TABLE_NAME, PlayersDB.PlayerEntry.COLUMN_NAME_CLUB_ID + "=" + idClub, null);
            db.delete(FixturesDB.FixtureEntry.TABLE_NAME, FixturesDB.FixtureEntry.COLUMN_NAME_CLUB_ID + "=" + idClub, null);
            makeJsonObjectRequestForPlayers();
            makeJsonObjectRequestForFixtures();
        } else {
            displayDataFromDatabase(idClub);
        }

        String[] args = new String[]{idClub};
        Cursor cursor = db.rawQuery("SELECT * FROM league WHERE idClub=?", args);
        String extension="";
        if (cursor.moveToFirst()) {
            do {
                extension = cursor.getString(cursor.getColumnIndex("iconLink")).substring(cursor.getString(cursor.getColumnIndex("iconLink")).length() - 3);
                club = new ClubClass(cursor.getString(cursor.getColumnIndex("idClub")),cursor.getString(cursor.getColumnIndex("idLeague")),
                        cursor.getString(cursor.getColumnIndex("name")),cursor.getString(cursor.getColumnIndex("iconLink")),
                        cursor.getString(cursor.getColumnIndex("marketValue")));
            } while (cursor.moveToNext());
        }
        if(extension==""){
            Toast.makeText(getActivity().getApplicationContext(),"Aucune image.", Toast.LENGTH_SHORT).show();
        } else if(extension=="svg"){
            Toast.makeText(getActivity().getApplicationContext(), "SVG", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), ""+extension, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.club_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
           // ((TextView) rootView.findViewById(R.id.club_detail)).setText(mItem.details);
        }

        return rootView;
    }

    public void displayDataFromDatabase(String idClub) {

        DBHelper mDbHelper = new DBHelper(getActivity().getApplicationContext());
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

        db.close();

        listPlayers=listOfPlayer;

        //Log.d("Player from database","IL y en a"+listOfPlayer.size());
        Toast.makeText(getActivity().getApplicationContext(), listOfPlayer.size() + " joueurs récupérées depuis la base de données.", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getActivity().getApplicationContext(), listOfFixture.size()+" fixtures récupérées depuis la base de données.", Toast.LENGTH_SHORT).show();
        //Log.d("Fixture form database","-> IL y en a "+listOfFixture.size());
        cursorFix.close();
        listFixtures=listOfFixture;
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
                DBHelper mDbHelper = new DBHelper(getActivity().getApplicationContext());
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
                    listPlayers=listOfPlayer;
                    // Log.d("Player from internet", "-> il y en a " + listOfPlayer.size());
                    Toast.makeText(getActivity().getApplicationContext(), listOfPlayer.size()+" joueurs récupérées depuis l'API.", Toast.LENGTH_SHORT).show();


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
                Toast.makeText(getActivity().getApplicationContext(),
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

                DBHelper mDbHelper = new DBHelper(getActivity().getApplicationContext());
                // Gets the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                // Create a new map of values, where column names are the keys


                try {
                    // Parsing json object response
                    // response will be a json object

                    //Toast.makeText(getActivity().getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
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
                    listFixtures=listOfFixture;
                    //Log.d("Fixture form internet","-> il y en a "+listOfFixture.size());
                    Toast.makeText(getActivity().getApplicationContext(), listOfFixture.size()+" fixtures récupérées depuis l'API.", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(getActivity().getApplicationContext(),
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
