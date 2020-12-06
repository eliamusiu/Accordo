package com.example.accordo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.accordo.Utils.getBase64FromBitmap;

public class SendImageActivity extends AppCompatActivity {
    private static final String TAG = SendImageActivity.class.toString();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);

        String ctitle = getIntent().getStringExtra("ctitle");

        Bitmap imageBitmap = Utils.getBitmapFromUri(getIntent().getParcelableExtra("imagePath"), getContentResolver());
        ((ImageView)findViewById(R.id.pickedImageImageView)).setImageBitmap(imageBitmap);

        // Listener con callback per inviare l'immagine al server trasformandola in base64
        findViewById(R.id.sendImageButton).setOnClickListener(v -> {
            String base64Image = Utils.getBase64FromBitmap(imageBitmap);
            CommunicationController cc = new CommunicationController(this);
            try {
                cc.addPost(ctitle, base64Image, "i",
                        response -> super.onBackPressed(),
                        error -> {
                            Context context = getApplicationContext();
                            CharSequence text = "Errore invio immagine"; //TODO: fare strings
                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(context, text, duration);toast.show();
                            Log.e(TAG, "Errore invio immagine: " + error.networkResponse);
                            super.onBackPressed();
                        } );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
}