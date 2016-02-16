package com.example.antoine.testapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.antoine.testapp.database.ClubDB;
import com.example.antoine.testapp.database.DBHelper;
import com.example.antoine.testapp.dummy.Club;
import com.example.antoine.testapp.dummy.LeagueClubs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity representing a list of Clubs. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ClubDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ClubListActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    // temporary string to show the parsed response
    private String jsonResponse;

    private View recyclerView;
    private String leagueId = "";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        Intent intent = getIntent();
        if(intent.hasExtra("league")) {
            String leagueExtra = intent.getExtras().get("league")+"";
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("league", leagueExtra);
            editor.commit();
            if(!leagueExtra.equals(leagueId)) {
                LeagueClubs.clear();
            }
            leagueId = leagueExtra;
        }else{
            String defaultValue = "";
            leagueId = sharedPref.getString("league", defaultValue);
        }

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.club_list);
        assert recyclerView != null;
        //setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.club_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Chargement des équipes...");
        pDialog.setCancelable(false);

        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        if(ServiceNetwork.isInternetAvailable(getApplicationContext())){
            //Log.d("TEST", "INTERNET");
            //db.delete(ClubDB.ClubEntry.TABLE_NAME, ClubDB.ClubEntry.COLUMN_NAME_LEAGUE_ID + "=" + leagueId, null);
            makeJsonObjectRequest();
        } else {
            //Log.d("TEST", "PAS INTERNET");
            displayDataFromDatabase(leagueId);
            setupRecyclerView((RecyclerView) recyclerView);
        }


        /*if(LeagueClubs.isEmpty())
            makeJsonObjectRequest();
        else
            setupRecyclerView((RecyclerView) recyclerView);*/
    }

    public void displayDataFromDatabase(String leagueId){
        LeagueClubs.clear();

        DBHelper mDbHelper = new DBHelper(getApplicationContext());
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String[] args = new String[] { leagueId };
        Cursor cursor = db.rawQuery("SELECT * FROM league WHERE idLeague=?", args);
        if (cursor.moveToFirst()){
            int i=0;
            do{
                String nameClub = cursor.getString(cursor.getColumnIndex("name"));
                String idClub = cursor.getString(cursor.getColumnIndex("idClub"));
                LeagueClubs.addItem(new Club((i + 1) + "", nameClub, "Club : "+nameClub, idClub));
                i++;
            }while(cursor.moveToNext());
            Toast.makeText(getApplicationContext(), i+1+" equipes récupérées depuis la base de données.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Liste équipe pour cette league vide en base de données.", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, ClubListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(LeagueClubs.ITEMS));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Club> mValues;

        public SimpleItemRecyclerViewAdapter(List<Club> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.club_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).nom);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("LID",mValues.get(position).idApi);
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ClubDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        arguments.putString("idClub", holder.mItem.idApi);
                        ClubDetailFragment fragment = new ClubDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.club_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ClubDetailActivity.class);
                        intent.putExtra(ClubDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        intent.putExtra("idClub", holder.mItem.idApi);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public Club mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }


    /**
     * Method to make json object request where json response starts wtih {
     */
    private void makeJsonObjectRequest() {

        showpDialog();

        LeagueClubs.clear();


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                "http://api.football-data.org/v1/soccerseasons/"+ leagueId + "/teams", null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, response.toString());
                DBHelper mDbHelper = new DBHelper(getApplicationContext());
                // Gets the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                // Create a new map of values, where column names are the keys

                try {
                    JSONArray array = response.getJSONArray("teams");

                    int nbTeam=0;
                    for(int i = 0; i < array.length(); i++){
                        String idClub = array.getJSONObject(i).getJSONObject("_links").getJSONObject("self").getString("href").replace("http://api.football-data.org/v1/teams/","");

                        String iconLink="";

                        String[] args = new String[] { idClub };
                        Cursor cursor = db.rawQuery("SELECT * FROM league WHERE idClub=?", args);
                        if (cursor.moveToFirst()) {
                            do {
                                iconLink = cursor.getString(cursor.getColumnIndex("iconLink"));
                            } while (cursor.moveToNext());
                        }
                        cursor.close();

                        db.delete(ClubDB.ClubEntry.TABLE_NAME, ClubDB.ClubEntry.COLUMN_NAME_CLUB_ID + "=" + idClub, null);


                        String extension = array.getJSONObject(i).getString("crestUrl").substring(array.getJSONObject(i).getString("crestUrl").length() - 3);
                        String nameFile=array.getJSONObject(i).getString("name")+"."+extension;

                        if(iconLink==""){
                            nameFile=nameFile.replace(" ", "_");
                            //Log.d("NOM FICHIER", nameFile);
                            Log.d("FILEDIR", ""+getApplicationContext().getFilesDir());
                            File file = new File(getApplicationContext().getFilesDir(),nameFile);

                            if(Utils.downloadFile(getApplicationContext(), array.getJSONObject(i).getString("crestUrl"), file)){
                                Log.e("OK", "ok");
                            } else {
                                Log.e("Erreyr", "erreyr");
                            }
                        }


                        ContentValues values = new ContentValues();
                        values.put(ClubDB.ClubEntry.COLUMN_NAME_LEAGUE_ID, leagueId);
                        values.put(ClubDB.ClubEntry.COLUMN_NAME_TEAM_NAME, array.getJSONObject(i).getString("name"));
                        values.put(ClubDB.ClubEntry.COLUMN_NAME_ICON_LINK, nameFile);
                        values.put(ClubDB.ClubEntry.COLUMN_NAME_MARKET_VALUE, array.getJSONObject(i).getString("squadMarketValue"));
                        //values.put(ClubDB.ClubEntry.COLUMN_NAME_CLUB_FIXTURES, array.getJSONObject(i).getJSONObject("_links").getJSONObject("fixtures").getString("href"));
                        //values.put(ClubDB.ClubEntry.COLUMN_NAME_CLUB_PLAYERS, array.getJSONObject(i).getJSONObject("_links").getJSONObject("players").getString("href"));
                        values.put(ClubDB.ClubEntry.COLUMN_NAME_CLUB_ID, idClub);

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId;

                        newRowId = db.insert(
                                    ClubDB.ClubEntry.TABLE_NAME,
                                    null,
                                    values);
                        String name = array.getJSONObject(i).getString("name");
                        LeagueClubs.addItem(new Club((i + 1) + "", name, "Club : " + name, idClub));
                        nbTeam++;
                        //Toast.makeText(getApplicationContext(), array.getJSONObject(i).getString("name"), Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getApplicationContext(), nbTeam+" equipes récupérées depuis l'API.", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    /*Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();*/
                } finally {
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
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
        setupRecyclerView((RecyclerView) recyclerView);
    }
}
