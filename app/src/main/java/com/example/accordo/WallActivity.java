package com.example.accordo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;

public class WallActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    final String TAG = WallActivity.class.toString();
    private static final int ACTION_REQUEST_GALLERY = 1;
    private CommunicationController cc;
    private SwipeRefreshLayout wallSwipeRefreshLayout;
    private ProfileBottomSheetFragment bottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);

        // Accesso implicito
        setSidFromSharedPreferences();      // Prende il sid dalle shared preferences
        if (Model.getInstance(this).getSid() == null ||                                 // Se non c'è il sid
                Model.getInstance(this).getSid().equals("no")) {                        // o se è il default value
            getSid();                       // Richiesta di rete per ottenere il sid
        } else {                                                                    // Se c'è
            getWall();
        }

        // Gestore evento sulla barra di navigazione (modifica profilo)
        ((BottomNavigationView)findViewById(R.id.bottom_navigation)).setOnNavigationItemSelectedListener( item -> {
                if (item.getItemId() == R.id.profile_page) {
                    bottomSheetFragment = ProfileBottomSheetFragment.newInstance(this);
                    bottomSheetFragment.show(getSupportFragmentManager(), ProfileBottomSheetFragment.TAG);
                }
            return false;
        });

        // Gestore evento su floating action button (aggiungi canale)
        findViewById(R.id.fab).setOnClickListener( v -> {
            DialogFragment fragment = new AddChannelFragment();
            fragment.show(getSupportFragmentManager(), " Add channel fragment");
        });

        // Gestore evento swipe to refresh (aggiorna canali)
        wallSwipeRefreshLayout = findViewById(R.id.wallSwiperefresh);
        wallSwipeRefreshLayout.setOnRefreshListener(
                () -> getWall()
        );
    }

    /**
     * Prende il sid dalla memoria (sharedPreferences) e lo salva nel model
     */
    private void setSidFromSharedPreferences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.preference_file_sid_default_value);
        Model.getInstance(this).setSid(sharedPref.getString(getString(R.string.preference_file_sid), defaultValue));
    }

    /**
     * Fa la richiesta di rete per ottenere il sid, quando viene ricevuto, nella callback chiama
     * {@link #setSharedPreference(String)} per salvarlo nelle sharedPreferences e chiama
     * {@link #getWall()} per ottenere i canali
     */
    private void getSid() {
        cc = new CommunicationController(this);
        cc.register(response -> {
            try {
                setSharedPreference((String) response.get("sid"));
                // TODO: fare toast per avvisare l'ottnimento del sid
                getWall();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "request correct: " + response.toString());
        }, error -> Log.d(TAG, "request error: " + error.toString())
        );
    }

    /**
     * Salva il sid nelle sharedPreferences e nel Model
     * @param sid sid da salvare nelle shared preferences e nel Model
     */
    private void setSharedPreference(String sid) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preference_file_sid), sid);
        Model.getInstance(this).setSid(sid);
        editor.apply();
    }

    /**
     * Fa richiesta di rete per ottenere i canali. Nella callback li aggiunge al model e chiama
     * {@link #setRecyclerView()}
     */
    private void getWall() {
        cc = new CommunicationController(this);
        try {
            cc.getWall(
                    // TODO: fare metodo separato
                    response -> {
                        try {
                            Model.getInstance(this).addChannels(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setRecyclerView();
                        wallSwipeRefreshLayout.setRefreshing(false);
                    },
                    error -> Log.d(TAG, "request error: " + error.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setta la recyclerView: layout e adapter
     */
    private void setRecyclerView() {
        RecyclerView rv = findViewById(R.id.wallRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ChannelAdapter adapter = new ChannelAdapter(this, this);
        rv.setAdapter(adapter);
    }

    /**
     * Gestore evento di click su un elemento (canale) della recyclerView
     * @param v Elemento della recyclerView (il viewHolder) che è stato cliccato
     * @param position Posizione dell'elemento cliccato sulla lista
     */
    @Override
    public void onRecyclerViewClick(View v, int position) {
        Intent intent = new Intent(getApplicationContext(), ChannelActivity.class);
        String ctitle = Model.getInstance(this).getChannel(position).getCtitle();
        intent.putExtra("ctitle", ctitle);
        startActivity(intent);
    }

    /**
     * Mostra il file managaer per scegliere l'immagine profilo nel profileBottomSheetFragment
     */
    public void onEditProfileImageClick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Scegli immagine"), 1);
    }

    /**
     * Viene chiamata quando si è scelta l'immagine dal file manager
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED && resultCode == RESULT_OK && requestCode == ACTION_REQUEST_GALLERY) {
            bottomSheetFragment.setProfilePicture(data.getData());
        }
    }
}