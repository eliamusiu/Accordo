package com.example.accordo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class ChannelActivity extends AppCompatActivity implements OnRecyclerViewClickListener {
    private final String TAG = ChannelActivity.class.toString();
    private CommunicationController cc;
    private String ctitle;
    SwipeRefreshLayout postsSwipeRefreshLayout;
    private List<Bitmap> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        androidx.appcompat.widget.Toolbar myToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.channelToolbar);
        setSupportActionBar(myToolbar);

        // Prende l'indice del'elemento della RecyclerView che è stato cliccato
        Intent intent = getIntent();
        ctitle = intent.getStringExtra("ctitle");
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

        findViewById(R.id.attachButton).setOnClickListener(v -> {
            PopupAttach popupAttach = new PopupAttach();
            popupAttach.showPopupWindow(v, findViewById(R.id.postConstraintLayout), this);
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
        images.clear();
        cc = new CommunicationController(this);
        ArrayList<TextImagePost> imagePosts = Model.getInstance().getAllImagePosts();
        if (imagePosts.size() == 0) {
            setRecyclerView();
        }
        for (TextImagePost post : imagePosts) {
            cc.getPostImage(post.getPid(),
                    reponse -> {
                        try {
                            String content = reponse.getString("content");
                            post.setContent(content);
                            images.add(Utils.getBitmapFromBase64(content));
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
        ImageView contentImageView = (ImageView)v;
        Collections.reverse(images);
        String imageContent = ((TextImagePost)Model.getInstance().getPost(position)).getContent();
        int imagePosition = Utils.getBitmapPositionInList(images, Utils.getBitmapFromBase64(imageContent));
        new StfalconImageViewer.Builder<>(this, images, new ImageLoader<Bitmap>() {
            @Override
            public void loadImage(ImageView imageView, Bitmap image) {
                Glide.with(getApplicationContext())
                        .load(image)
                        .into(imageView);
            }
        }).withStartPosition(imagePosition).withTransitionFrom(contentImageView).show();
        // TODO: immagini al contrario nel carousel
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

    private static final int ACTION_REQUEST_CAMERA = 0;
    private static final int ACTION_REQUEST_GALLERY = 1;


    /* Click su tipo allegato (immagine o posizione) nel popupmenu */
    public void onClick() {
        selectImage(this);
    }

    public void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(ChannelActivity.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Cancel")) {
                dialog.dismiss();
            } else {
                Intent intent = new Intent(ChannelActivity.this, ImagePickActivity.class);
                intent.putExtra("optionSelected", options[item]);
                intent.putExtra("ctitle", ctitle);
                startActivity(intent);
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageView = (ImageView) findViewById(R.id.pickedImageImageView);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case ACTION_REQUEST_CAMERA:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedImage);
                    }
                    break;
                case ACTION_REQUEST_GALLERY:
                    if (resultCode == RESULT_OK) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(data.getData());
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            imageView.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
    }
}