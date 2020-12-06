package com.example.accordo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.stfalcon.imageviewer.StfalconImageViewer;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChannelActivity extends AppCompatActivity implements OnPostRecyclerViewClickListener {
    private final String TAG = ChannelActivity.class.toString();
    private CommunicationController cc;
    private String ctitle;
    SwipeRefreshLayout postsSwipeRefreshLayout;
    private List<Bitmap> images = new ArrayList<>();
    private static final int ACTION_REQUEST_GALLERY = 1;
    private PostAdapter adapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        // Setta la toolbar (titolo in alto della Activity)
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.channelToolbar);
        setSupportActionBar(myToolbar);

        // Prende l'indice del'elemento della RecyclerView dei canali della WallActivity
        // che è stato cliccato
        Intent intent = getIntent();
        ctitle = intent.getStringExtra("ctitle");
        getSupportActionBar().setTitle(ctitle);             // Setta il titolo della toolbar con il nome del canale

        // Gestore evento di click sul bottone di invio del post
        findViewById(R.id.sendButton).setOnClickListener(v -> {
            try {
                addPost();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // Gestore evento di swipe to refresh per aggiornare i post
        postsSwipeRefreshLayout = findViewById(R.id.postsSwiperefresh);
        postsSwipeRefreshLayout.setOnRefreshListener(
                () -> getPosts()
        );

        // Gestore evento di click sul bottone "Allega" per mostrare il popUp con la sceltra tra immagine e posizione
        findViewById(R.id.attachButton).setOnClickListener(v -> {
            PopupAttach popupAttach = new PopupAttach();
            popupAttach.showPopupWindow(v, findViewById(R.id.postConstraintLayout), this);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPosts();
    }

    /**
     * Fa la richiesta di rete del {@link CommunicationController} per ottenere i post
      */
    private void getPosts() {
        cc = new CommunicationController(this);
        try {
            cc.getChannel(ctitle,
                    response -> {
                        try {
                            Model.getInstance(this).addPosts(response);     // Setta i post testo
                            setRecyclerView();
                            getUserPictures();                              // Setta le immagini profilo degli utenti
                            getImages();                                    // Setta i post immagine
                            postsSwipeRefreshLayout.setRefreshing(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d(TAG, "request error: " + error.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chiama il metodo {@link ProfilePictureController#setProfilePictures(Runnable)}
     */
    private void getUserPictures() {
        ProfilePictureController ppc = new ProfilePictureController(this);
        ppc.setProfilePictures(() -> adapter.notifyData());
    }

    /**
     * Fa la richiesta di rete del {@link CommunicationController} per ottenere le immagini e nella
     * callback le aggiunge sia al {@link Model} sia a {@link #images}. Alla fine chiama
     * {@link PostAdapter#notifyData()}
     * @throws JSONException
     */
    private void getImages() throws JSONException {
        images.clear();
        cc = new CommunicationController(this);
        ArrayList<TextImagePost> imagePosts = Model.getInstance(this).getAllImagePosts();

        for (TextImagePost post : imagePosts) {
            cc.getPostImage(post.getPid(),
                    reponse -> {
                        try {
                            String content = reponse.getString("content");
                            post.setContent(content);
                            images.add(Utils.getBitmapFromBase64(content));
                            if (imagePosts.indexOf(post) == (imagePosts.size() - 1)) { // se è l'ultimo post con immagine viene settata la recycler view
                                adapter.notifyData();
                                Collections.reverse(images);
                                rv.scrollToPosition(0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d(TAG, "request error: " + error.toString())
            );
        }
    }

    /**
     * Fa la richiesta di rete del {@link CommunicationController} per aggiungere un post di tipo
     * testo, prendendolo dalla EditText
     * @throws JSONException
     */
    private void addPost() throws JSONException {
        String postText = ((EditText)findViewById(R.id.postEditText)).getText().toString();
        cc = new CommunicationController(this);
        cc.addPost(ctitle, postText, Post.TEXT,
                response -> {
                    ((EditText)findViewById(R.id.postEditText)).setText("");
                    getPosts();
                },
                error -> Log.e(TAG, "Errore aggiunta post: " + error));
    }

    /**
     * Setta {@link #rv} impostando il layout in modo che sia inverso (post più recente in basso)
     * e scorrere automaticamente {@link #rv} in basso
     */
    private void setRecyclerView() {
        rv = findViewById(R.id.postsRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rv.setLayoutManager(linearLayoutManager);
        rv.scrollToPosition(0);
        adapter = new PostAdapter(this, this);
        rv.setAdapter(adapter);
    }

    /**
     * Gestore dell'evento di click sull'immagine di un post di tipo immagine.
     * Mpstra l'immagine a schermo intero tramite {@link StfalconImageViewer.Builder} e, scorrendp,
     * le immagini degli altri post, salvate in {@link #images}
     * @param v {@link ImageView} che è stata cliccata
     * @param position Posizione dell'elemento cliccato in {@link #rv}
     */
    @Override
    public void onRecyclerViewImageClick(View v, int position) {
        ImageView contentImageView = (ImageView)v;
        String imageContent = ((TextImagePost)Model.getInstance(this).getPost(position)).getContent();
        int imagePosition = Utils.getBitmapPositionInList(images, Utils.getBitmapFromBase64(imageContent));     // TODO: se le immagini sono doppie prende sempre la prima
        new StfalconImageViewer.Builder<>(this, images, (imageView, image) -> Glide.with(getApplicationContext())
                .load(image)
                .into(imageView)).withStartPosition(imagePosition).withTransitionFrom(contentImageView).show();
    }

    /**
     * Gestore dell'evento di click sulla posizione di un posti di tipo posizione. Fa partire
     * {@link SendImageActivity} passandole la posizione del post in {@link #rv}
     * @param v {@link android.widget.LinearLayout} della posizione che è stato cliccato
     * @param position Posizione dell'elemento cliccato in {@link #rv}
     */
    @Override
    public void onRecyclerViewLocationClick(View v, int position) {
        Intent intent = new Intent(ChannelActivity.this, SendLocationActivity.class);
        intent.putExtra("postIndex", position);
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * Gestore evento di click su bottone della {@link android.widget.Toolbar} (refresh)
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            postsSwipeRefreshLayout.setRefreshing(true);
            getPosts();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /* C */

    /**
     * Gestore evento di lick su tipo allegato (immagine o posizione) in {@link PopupAttach}.
     * Crea la relativa activity: file manager o {@link SendLocationActivity}
     * @param type Tipo di allegato: "i" immagine, "l" posizione
     */
    public void onAttachClick(String type) {
        if (type.equals("i")) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Scegli immagine"), 1);
        } else if (type.equals("l")){
            Intent intent = new Intent(ChannelActivity.this, SendLocationActivity.class);
            intent.putExtra("ctitle", ctitle);
            startActivity(intent);
        } else {
            Log.d(TAG, "no good");
        }
    }

    /**
     * Chiamata quando viene selezionata l'immagine da inviare nel file manager. Apre
     * {@link SendImageActivity} per inviare l'immagine
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED && resultCode == RESULT_OK && requestCode == ACTION_REQUEST_GALLERY) {
            Intent intent = new Intent(ChannelActivity.this, SendImageActivity.class);
            intent.putExtra("imagePath", data.getData());
            intent.putExtra("ctitle", ctitle);
            startActivity(intent);
        }
    }
}
