package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.accordo.Utils.getBase64FromBitmap;

public class SendImageActivity extends AppCompatActivity {
    private static final String TAG = SendImageActivity.class.toString();

    String ctitle;
    String base64Image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);

        setToolbar();

        ctitle = getIntent().getStringExtra("ctitle");

        Bitmap imageBitmap = Utils.getBitmapFromUri(getIntent().getParcelableExtra("imagePath"), getContentResolver());
        base64Image = Utils.getBase64FromBitmap(imageBitmap);
        ((ImageView)findViewById(R.id.pickedImageImageView)).setImageBitmap(imageBitmap);

        if (base64Image.length() <= CommunicationController.MAX_IMAGE_LENGTH) {
            // Listener con callback per inviare l'immagine al server trasformandola in base64
            findViewById(R.id.sendImageButton).setOnClickListener(v -> {
                sendImage();
            });
        } else {
            findViewById(R.id.sendImageButton).setVisibility(View.GONE);
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.pickedImageImageView), R.string.image_too_large_message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAnchorView(R.id.sendImageButton)
                    .setAction("OK", v -> super.onBackPressed())
                    .show();
        }
    }

    private void sendImage() {
        CommunicationController cc = new CommunicationController(this);
        try {
            cc.addPost(ctitle, base64Image, Post.IMAGE,
                    response -> super.onBackPressed(),
                    error -> Log.e(TAG, "Errore invio immagine: " + error.networkResponse));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setToolbar() {
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.sendImageToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}