package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;

public class ChannelActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    private final String TAG = ChannelActivity.class.toString();
    private CommunicationController cc;
    private String ctitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        // Prende l'indice del'elemento della RecyclerView che è stato cliccato
        Intent intent = getIntent();
        int index = intent.getIntExtra("channelIndex", -1);
        ctitle = Model.getInstance().getChannel(index).getCtitle();
        getPosts();

        findViewById(R.id.sendButton).setOnClickListener(v -> {
            ((EditText)findViewById(R.id.postEditText)).setText("");
            try {
                addPost();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    // Richiesta di rete per ottenere i post
    private void getPosts() {
        cc = new CommunicationController(this);
        try {
            cc.getPosts(ctitle,
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

    private void addPost() throws JSONException {
        String postText = ((EditText)findViewById(R.id.postEditText)).getText().toString();
        cc = new CommunicationController(this);
        cc.addPost(ctitle, postText, "t",
                response -> getPosts(),
                error -> Log.e(TAG, "Errore aggiunta post: " + error));
    }

    private void setRecyclerView() {
        RecyclerView rv = findViewById(R.id.postsRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        rv.setLayoutManager(linearLayoutManager);
        rv.scrollToPosition(0);
        PostAdapter adapter = new PostAdapter(this, this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onRecyclerViewClick(View v, int position) {
        // TODO: gestire l'apertura dell'immagine o della posizione
    }
}