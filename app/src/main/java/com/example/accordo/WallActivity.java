package com.example.accordo;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WallActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    final String TAG = WallActivity.class.toString();
    private static final int ACTION_REQUEST_GALLERY = 1;
    private CommunicationController cc;
    private SwipeRefreshLayout wallSwipeRefreshLayout;
    private RecyclerView rv;
    private ProfileBottomSheetFragment bottomSheetFragment;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall);

        manageInternetConnectionCheck();

        // Gestore evento sulla barra di navigazione (modifica profilo)
        ((BottomNavigationView)findViewById(R.id.bottom_navigation)).setOnNavigationItemSelectedListener( item -> {
                if (item.getItemId() == R.id.profile_page) {
                    bottomSheetFragment = ProfileBottomSheetFragment.newInstance();
                    bottomSheetFragment.show(getSupportFragmentManager(), ProfileBottomSheetFragment.TAG);
                } else if (item.getItemId() == R.id.home_page) {
                    rv.scrollToPosition(0);
                }
            return false;
        });

        // Gestore evento su floating action button (aggiungi canale)
        findViewById(R.id.fab).setOnClickListener( v -> createDialog());

        // Gestore evento swipe to refresh (aggiorna canali)
        wallSwipeRefreshLayout = findViewById(R.id.wallSwiperefresh);
        wallSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Utils.getThemeAttr(R.attr.colorPost, this));
        wallSwipeRefreshLayout.setColorSchemeColors(Utils.getThemeAttr(R.attr.colorPrimary, this));
        wallSwipeRefreshLayout.setOnRefreshListener(
                () -> getWall(false)
        );

        setToolbar();
    }

    /**
     * Se il device è connesso a internet chiama {@link #checkSidInStorage()}, altrimenti mostra un dialog
     * che richiede di attivare la connessione a Internet
     */
    private void manageInternetConnectionCheck() {
        if (Utils.isNetworkAvailable(this)) {
            checkSidInStorage();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            // Setta l'interfaccia
            builder.setTitle(R.string.unavailable_network_connection);
            builder.setMessage(R.string.turn_on_network_connection);
            // Gestori eventi sui pulsanti del dialog
            builder.setPositiveButton(R.string.done_positive_button, (dialog, which) -> manageInternetConnectionCheck());
            builder.show();
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.wallToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    /**
     * Controlla se il SID è presente nelle shared preferences e, in tal caso, chiama {@link #getWall(boolean)},
     * altrimenti lo richiede al server
     */
    private void checkSidInStorage() {
        // Accesso implicito
        setSidFromSharedPreferences();      // Prende il sid dalle shared preferences
        if (Model.getInstance(this).getSid() == null ||                                 // Se non c'è il sid
                Model.getInstance(this).getSid().equals(R.string.preference_file_sid_default_value)) { // o se è il default value
            getSid();                       // Richiesta di rete per ottenere il sid
        } else {                                                                    // Se c'è
            getWall(true);
            getActualUserProfile(); //ottiene l'utente attuale
        }
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
     * {@link #getWall(boolean)}} per ottenere i canali
     */
    private void getSid() {
        cc = new CommunicationController(this);
        cc.register(response -> {
            try {
                setSharedPreference((String) response.get("sid"));
                // Snackbar
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.bottomMenu),R.string.new_user_created, Snackbar.LENGTH_LONG);
                snackbar.setAnchorView(R.id.fab)
                        .show();
                getActualUserProfile(); //ottiene l'utente attuale
                getWall(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "request correct: " + response.toString());
        }, error -> Log.e(TAG, "request error: " + error.toString())
        );
    }

    /**
     * Fa la richiesta di rete per ottenere l'utente attuale e, nella callback, salva la risposta in
     * {@link Model#setActualUser(User)}
     */
    public void getActualUserProfile() {
        // Prende le informazioni dell'utente dal server per poi mostrarle nell'imageView e nell'editText
        try {
            cc = new CommunicationController(this);
            cc.getProfile(response -> {
                        Gson gson = new Gson();
                        Model.getInstance(this).setActualUser(gson.fromJson(response.toString(), User.class));
                    },
                    error -> Log.e(TAG, "Errore richiesta: " + error));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
     * @param firstTime Indica se è la prima volta che si ottiene la bacheca, per non settare più
     *                  volte {@link #addRecyclerViewItemDecoration()}, altrimenti si sommano
     *                  togliendo la trasparenza di volta in volta
     */
    private void getWall(boolean firstTime) {
        cc = new CommunicationController(this);
        try {
            cc.getWall(
                    response -> {
                        try {
                            Model.getInstance(this).addChannels(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        setRecyclerView();
                        if (firstTime) {
                            addRecyclerViewItemDecoration();
                        }
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
        rv = findViewById(R.id.wallRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        ChannelAdapter adapter = new ChannelAdapter(this, this);
        rv.setAdapter(adapter);

    }

    private void addRecyclerViewItemDecoration() {
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext());
        rv.removeItemDecoration(dividerItemDecoration);
        rv.addItemDecoration(dividerItemDecoration);
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

    /**
     * Crea e setta l'interfaccia del {@link MaterialAlertDialogBuilder}
     */
    private void createDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        // Setta l'interfaccia
        builder.setTitle(R.string.new_channel);
        builder.setView(R.layout.fragment_add_channel);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_channel, null);
        builder.setView(view);
        // Gestori eventi sui pulsanti del dialog
        builder.setPositiveButton(R.string.add_positive_button, (dialog, which) -> addChannel(view));
        builder.show();
    }

    /**
     * Fa la richiesta di rete del {@link CommunicationController} prendendo il nome del canale
     * dalla EditText
     * @param view
     */
    private void addChannel(View view) {
        CommunicationController cc = new CommunicationController(this);
        final EditText et = view.findViewById(R.id.ctitleTextView);
        String ctitle = et.getText().toString();

        try {
            cc.addChannel(ctitle,
                    response -> {
                        Log.d(TAG, "Canale creato");
                        getWall(false);
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.bottomMenu),"Canale " + ctitle + " creato correttamente", Snackbar.LENGTH_LONG);
                        snackbar.setAnchorView(findViewById(R.id.fab))
                                .show();
                    },
                    error -> {
                        Log.d(TAG, "Errore creazione canale: " + error.toString());
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.bottomMenu),R.string.channel_creation_error, Snackbar.LENGTH_LONG);
                        snackbar.setAnchorView(findViewById(R.id.fab))
                                .show();
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

