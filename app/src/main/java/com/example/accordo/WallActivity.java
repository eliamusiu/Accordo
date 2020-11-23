package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;

public class WallActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    final String TAG = String.valueOf(WallActivity.class);
    final String sid = "35vzqlT9QKUogwn6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CommunicationController cc = new CommunicationController(this);
        /*
        cc.register(response -> {
            Log.d(TAG, "request correct: " + response.toString());
        }, error -> {
            Log.d(TAG, "request error: " + error.toString());
        });
        */

        // Richiesta di rete per ottenere l'elenco dei canali
        try {
            cc.getWall(sid,
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

        // Gestore evento sulla barra di navigazione
        ((BottomNavigationView)findViewById(R.id.bottom_navigation)).setOnNavigationItemSelectedListener( item -> {
                if (item.getItemId() == R.id.profile_page) {
                    View view = getLayoutInflater().inflate(R.layout.fragment_profile_bottom_sheet, null);

                    BottomSheetDialog dialog = new BottomSheetDialog(this);
                    dialog.setContentView(view);
                    dialog.show();
                }
            return false;
        });

        // Gestore evento su floating action button (aggiungi canale)
        findViewById(R.id.fab).setOnClickListener( v -> {
            DialogFragment fragment = new AddChannelFragment();
            Bundle bundle = new Bundle();

            bundle.putString("sid", sid);
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), " Add channel fragment");
        });
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