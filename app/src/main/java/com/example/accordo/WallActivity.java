package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;

public class WallActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    final String TAG = WallActivity.class.toString();
    CommunicationController cc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);

        // Accesso implicito
        setSidFromSharedPreferences();      // Prende il sid dalle shared preferences
        //setUidFromSharedPreferences();
        if (Model.getInstance().getSid() == null ||                                 // Se non c'è il sid
                Model.getInstance().getSid().equals("no")) {                        // o se è il default value
            getSid();                       // Richiesta di rete per ottenere il sid
            //getUid();
        } else {                                                                    // Se c'è
            getWall();
        }

        // Gestore evento sulla barra di navigazione
        ((BottomNavigationView)findViewById(R.id.bottom_navigation)).setOnNavigationItemSelectedListener( item -> {
                if (item.getItemId() == R.id.profile_page) {
                    View view = getLayoutInflater().inflate(R.layout.fragment_profile_bottom_sheet, null);

                    ProfileBottomSheetFragment dialog = ProfileBottomSheetFragment.newInstance();
                    dialog.show(getSupportFragmentManager(), ProfileBottomSheetFragment.TAG);
                    ProfileBottomSheetFragment fragment = new ProfileBottomSheetFragment();
                }
            return false;
        });

        // Gestore evento su floating action button (aggiungi canale)
        findViewById(R.id.fab).setOnClickListener( v -> {
            DialogFragment fragment = new AddChannelFragment();
            fragment.show(getSupportFragmentManager(), " Add channel fragment");
        });
    }

    private void setSharedPreference(String sid) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preference_file_sid), sid);
        Model.getInstance().setSid(sid);
        editor.apply();
    }

    private void setSidFromSharedPreferences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.preference_file_sid_default_value);
        Model.getInstance().setSid(sharedPref.getString(getString(R.string.preference_file_sid), defaultValue));
    }

    private void getSid() {
        cc = new CommunicationController(this);
        cc.register(response -> {
            try {
                setSharedPreference((String) response.get("sid"));
                getWall();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "request correct: " + response.toString());
        }, error -> {
            Log.d(TAG, "request error: " + error.toString());
        });
    }
    /*
    private void setUidSharedPreference(String uid) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preference_file_uid), uid);
        Model.getInstance().setSid(uid);
        editor.apply();
    }

    private void setUidFromSharedPreferences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.preference_file_uid_default_value);
        Model.getInstance().setUid(sharedPref.getString(getString(R.string.preference_file_uid), defaultValue));
    }

    private void getUid() {
        try {
            cc = new CommunicationController(this);
            cc.getProfile(response -> {
                        try {
                            setUidSharedPreference((String) response.get("uid"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.e(TAG, "Errore richiesta: " + error));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    */

    private void getWall() {
        cc = new CommunicationController(this);
        try {
            cc.getWall(
                    // TODO: fare metodo separato
                    response -> {
                        try {
                            Model.getInstance().addChannels(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setRecyclerView();
                    },
                    error -> Log.d(TAG, "request error: " + error.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRecyclerView() {
        RecyclerView rv = findViewById(R.id.wallRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ChannelAdapter adapter = new ChannelAdapter(this, this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onRecyclerViewClick(View v, int position) {
        Intent intent = new Intent(getApplicationContext(), ChannelActivity.class);
        intent.putExtra("channelIndex", position);
        startActivity(intent);
    }
}