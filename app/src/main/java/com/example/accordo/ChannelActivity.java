package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChannelActivity extends AppCompatActivity implements OnRecyclerViewClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        Intent intent = getIntent();
        int index = intent.getIntExtra("channelIndex", -1);
        String ctitle = Model.getInstance().getChannel(index).getCtitle();
        try {
            final JSONObject jsonBody = new JSONObject();
            jsonBody.put("sid", "35vzqlT9QKUogwn6");
            jsonBody.put("ctitle", ctitle);
            getPostRequest("https://ewserver.di.unimi.it/mobicomp/accordo/getChannel.php", jsonBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPostRequest(String url, JSONObject jsonBody) throws JSONException {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        JsonObjectRequest request = new JsonObjectRequest(
                url,
                jsonBody,
                response -> {
                    try {
                        Model.getInstance().addPosts(response);
                        ArrayList<Post> posts = Model.getInstance().getAllPosts();
                        setRecyclerView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("Posts", "Errore " + error));

        // Add the request to the RequestQueue.
        queue.add(request);
    }

    private void setRecyclerView() {
        RecyclerView rv = findViewById(R.id.postsRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        Adapter adapter = new Adapter(this, Model.getInstance().getAllPosts(), "posts", this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onRecyclerViewClick(View v, int position) {
    }
}