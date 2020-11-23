package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;

public class ChannelActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    final String TAG = String.valueOf(WallActivity.class);
    final String sid = "35vzqlT9QKUogwn6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        // Prende l'indice del'elemento della RecyclerView che è stato cliccato
        Intent intent = getIntent();
        int index = intent.getIntExtra("channelIndex", -1);
        String ctitle = Model.getInstance().getChannel(index).getCtitle();

        // Richiesta di rete per ottenere i post
        CommunicationController cc = new CommunicationController(this);
        try {
            cc.getPosts(sid, ctitle,
                    response -> {
                        // TODO: fare metodo separato
                        try {
                            Model.getInstance().addPosts(response);
                            setRecyclerView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d(TAG, "request error: " + error.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setRecyclerView() {
        RecyclerView rv = findViewById(R.id.postsRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        PostAdapter adapter = new PostAdapter(this, this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onRecyclerViewClick(View v, int position) {
        // TODO: gestire l'apertura dell'immagine o della posizione
    }
}