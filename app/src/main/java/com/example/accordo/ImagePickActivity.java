package com.example.accordo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.example.accordo.Utils.getBase64FromBitmap;

public class ImagePickActivity extends AppCompatActivity {
    private static final int ACTION_REQUEST_CAMERA = 0;
    private static final int ACTION_REQUEST_GALLERY = 1;
    private static final String TAG = ImagePickActivity.class.toString();
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pick);

        String optionSelected = getIntent().getStringExtra("optionSelected");

        if (optionSelected.equals("Take Photo")) {
            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, ACTION_REQUEST_CAMERA);

        } else if (optionSelected.equals("Choose from Gallery")) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Scegli immagine"), ACTION_REQUEST_GALLERY);
        }

        findViewById(R.id.sendImageButton).setOnClickListener(v -> {
            String base64Image = getBase64FromBitmap(imageBitmap);
            String ctitle = getIntent().getStringExtra("ctitle");
            CommunicationController cc = new CommunicationController(this);
            try {
                cc.addPost(ctitle, base64Image, "i",
                        response -> {
                            Intent channelActivityIntent = new Intent(ImagePickActivity.this, ChannelActivity.class);
                            channelActivityIntent.putExtra("ctitle", ctitle);
                            startActivity(channelActivityIntent);
                        },
                        error -> Log.e(TAG, "Errore invio immagine: " + error.toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
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
                            imageBitmap = BitmapFactory.decodeStream(inputStream);
                            imageView.setImageBitmap(imageBitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
    }
}