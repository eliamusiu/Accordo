package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    private final String TAG = ChannelActivity.class.toString();
    private CommunicationController cc;
    private String ctitle;
    SwipeRefreshLayout postsSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        androidx.appcompat.widget.Toolbar myToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.channelToolbar);
        setSupportActionBar(myToolbar);

        // Prende l'indice del'elemento della RecyclerView che è stato cliccato
        Intent intent = getIntent();
        int index = intent.getIntExtra("channelIndex", -1);
        ctitle = Model.getInstance().getChannel(index).getCtitle();
        getSupportActionBar().setTitle(ctitle);
        getPosts();

        findViewById(R.id.sendButton).setOnClickListener(v -> {
            try {
                addPost();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        postsSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.postsSwiperefresh);
        postsSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getPosts();
                    }
                }
        );
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
                            getImages();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d(TAG, "request error: " + error.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getImages() throws JSONException {
        cc = new CommunicationController(this);
        ArrayList<TextImagePost> imagePosts = Model.getInstance().getAllImagePosts();
        for (TextImagePost post : imagePosts) {
            cc.getPostImage(post.getPid(),
                    reponse -> {
                        try {
                            post.setContent(reponse.getString("content"));
                            if (imagePosts.indexOf(post) == (imagePosts.size() - 1)) {
                                setRecyclerView();
                                postsSwipeRefreshLayout.setRefreshing(false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d(TAG, "request error: " + error.toString())
            );
        }
    }

    private void addPost() throws JSONException {
        String postText = ((EditText)findViewById(R.id.postEditText)).getText().toString();
        cc = new CommunicationController(this);
        cc.addPost(ctitle, postText, "t",
                response -> {
                    ((EditText)findViewById(R.id.postEditText)).setText("");
                    getPosts();
                },
                error -> Log.e(TAG, "Errore aggiunta post: " + error));
    }

    private void setRecyclerView() {
        RecyclerView rv = findViewById(R.id.postsRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rv.setLayoutManager(linearLayoutManager);
        rv.scrollToPosition(0);
        PostAdapter adapter = new PostAdapter(this, this);
        rv.setAdapter(adapter);
    }

    @Override
    public void onRecyclerViewClick(View v, int position) {
        // TODO: gestire l'apertura dell'immagine o della posizione
        View nextChild = ((ViewGroup) v).getChildAt(1);
        ImageView imageView = (ImageView)nextChild;
        //Glide.with(this).load(((BitmapDrawable)imageView.getDrawable()).getBitmap()).into(imageView);
        List<Drawable> images = new ArrayList<>();
        images.add(imageView.getDrawable());
        new StfalconImageViewer.Builder<>(this, images, new ImageLoader<Drawable>() {
            @Override
            public void loadImage(ImageView imageView, Drawable image) {
                Glide.with(getApplicationContext())
                        .load(image)
                        .into(imageView);
            }
        }).withTransitionFrom(imageView).show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                postsSwipeRefreshLayout.setRefreshing(true);
                getPosts();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}