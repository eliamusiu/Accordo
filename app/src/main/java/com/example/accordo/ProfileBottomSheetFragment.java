package com.example.accordo;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String TAG = ProfileBottomSheetFragment.class.toString();
    private CommunicationController cc;
    private ImageView profilePictureImageView;
    private EditText profileNameEditText;
    private Context context;
    private Bitmap croppedProfilePicBitmap = null;

    public ProfileBottomSheetFragment(Context context) {
        this.context = context;
        // Required empty public constructor
    }

    public static ProfileBottomSheetFragment newInstance(Context context) {
        return new ProfileBottomSheetFragment(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fa l'inflate del layout per questo fragment
        View view = inflater.inflate(R.layout.fragment_profile_bottom_sheet, container, false);
        profilePictureImageView = view.findViewById(R.id.userProfileImageView);
        profileNameEditText = view.findViewById(R.id.profileNameEditText);

        // Prende le informazioni dell'utente dal server per poi mostrarle nell'imageView e nell'editText
        try {
            cc = new CommunicationController(getContext());
            cc.getProfile(response -> {
                        setProfileInfo(response);
                    },
                    error -> Log.e(TAG, "Errore richiesta: " + error));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Click sull'immagine per cambiarla (apre il file explorer dalla WallActivity)
        profilePictureImageView.setOnClickListener(v -> {
            ((WallActivity)context).onEditProfileImageClick();
        });

        // Click sul bottone "modifica" per inviare al server le modifiche effettuate
        view.findViewById(R.id.editProfileButton).setOnClickListener(v -> {
            try {
                editProfile();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return view;
    }

    /**
     * Inserisce il nome dell'utente nella EditText e l'immagine del profilo nella ImageView
     * @param response Json contente "name" e "picture" del profilo
     */
    private void setProfileInfo(JSONObject response) {
        try {
            Object name = response.get("name");
            Object picture = response.get("picture");
            if (name != null) {
                profileNameEditText.setText(name.toString());
            }
            if (picture != null) {
                profilePictureImageView.setImageBitmap(Utils.getBitmapFromBase64(picture.toString()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fa la richiesta di rete per aggiornare nome dell'utente e/o immagine profilo
     * @throws JSONException
     */
    private void editProfile() throws JSONException {
        String name = profileNameEditText.getText().toString();
        String base64Image = null;

        if (croppedProfilePicBitmap != null) {
            base64Image = Utils.getBase64FromBitmap(croppedProfilePicBitmap);
        }
        cc = new CommunicationController(getContext());
        cc.setProfile(name, base64Image,
                response -> Log.d(TAG, "Richiesta di rete OK"),
                error -> Log.e(TAG, "Errore richiesta: " + error));     // TODO: Gestire errore 400 (nome già usato)
    }

    /**
     * Prende il bitmap dell'immagine, la fa diventare un quadrato (crop) e setta l'imageView del
     * profilo con essa
     * @param uri Uri dell'immagine di profilo
     */
    public void setProfilePicture(Uri uri) {
        Bitmap bitmap = Utils.getBitmapFromUri(uri, context.getContentResolver());
        croppedProfilePicBitmap = Utils.cropImageToSquare(bitmap);
        profilePictureImageView.setImageBitmap(croppedProfilePicBitmap);
    }
}