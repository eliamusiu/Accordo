package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    final String TAG = String.valueOf(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getChannelsRequest("https://ewserver.di.unimi.it/mobicomp/accordo/getWall.php");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((BottomNavigationView)findViewById(R.id.bottom_navigation)).setOnNavigationItemSelectedListener( item -> {
                if (item.getItemId() == R.id.profile_page) {
                    View view = getLayoutInflater().inflate(R.layout.fragment_profile_bottom_sheet, null);

                    BottomSheetDialog dialog = new BottomSheetDialog(this);
                    dialog.setContentView(view);
                    dialog.show();
                }
            return false;
        });

        ((FloatingActionButton)findViewById(R.id.fab)).setOnClickListener( v -> {
            DialogFragment fragment = new AddChannelFragment();
            fragment.show(getSupportFragmentManager(), " Add channel fragment");
        });
    }

    public void getChannelsRequest(String url) throws JSONException {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", "35vzqlT9QKUogwn6");

        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(
                url,
                jsonBody,
                response -> {
                    try {
                        Model.getInstance().addChannels(response);
                        setRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("Canali", "Errore " + error));

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void setRecyclerView() {
        RecyclerView rv = findViewById(R.id.wallRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        Adapter adapter = new Adapter(this, Model.getInstance().getAllChannels(), "channels", this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onRecyclerViewClick(View v, int position) {
        Intent intent = new Intent(getApplicationContext(), ChannelActivity.class);
        intent.putExtra("channelIndex", position);
        startActivity(intent);
    }
}