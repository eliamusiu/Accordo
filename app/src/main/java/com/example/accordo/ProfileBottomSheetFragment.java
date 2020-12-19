package com.example.accordo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String TAG = ProfileBottomSheetFragment.class.toString();
    private CommunicationController cc;
    private ImageView profilePictureImageView;
    private EditText profileNameEditText;
    private Button editProfileImageButton, editProfileButton;
    //private Context context;
    private Bitmap croppedProfilePicBitmap = null;

    public ProfileBottomSheetFragment() {
        // Required empty public constructor
    }

    public static ProfileBottomSheetFragment newInstance() {
        return new ProfileBottomSheetFragment();
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
        editProfileImageButton = view.findViewById(R.id.editProfileImageButton);
        editProfileButton = view.findViewById(R.id.editProfileButton);

        setProfileInfo();

        // Click sull'immagine per cambiarla (apre il file explorer dalla WallActivity)
        editProfileImageButton.setOnClickListener(v -> {
            ((WallActivity) getContext()).onEditProfileImageClick();
        });

        // Click sul bottone "modifica" per inviare al server le modifiche effettuate
        view.findViewById(R.id.editProfileButton).setOnClickListener(v -> {
            try {
                editProfile();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        //
        profileNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {  }

            @Override
            public void afterTextChanged(Editable s) {
                editProfileButton.setEnabled(true);
            }
        });
        return view;
    }

    /**
     * Espande il bottomSheetDialogFragment anche quando si ha il device in landscape
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            dialog.setOnShowListener(dialog1 -> {
                BottomSheetDialog d = (BottomSheetDialog) dialog1;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);

            });
        }
        return dialog;
    }
    /**
     * Inserisce il nome dell'utente nella EditText e l'immagine del profilo nella ImageView
     */
    private void setProfileInfo() {
        User actualUser = Model.getInstance(getContext()).getActualUser();
        Object name = actualUser.getName();
        Object picture = actualUser.getPicture();
        if (name != null) {
            profileNameEditText.setText(name.toString());
        }
        if (picture != null) {
            profilePictureImageView.setClipToOutline(true);
            profilePictureImageView.setImageBitmap(Utils.getBitmapFromBase64(picture.toString(), getContext()));
        }
    }

    /**
     * Prende il testo dalla EditText e, se presente, trasforma l'immagine in bitmap.
     * Passa a {@link #sendEditProfileRequest(String, String)} queste informazioni appena prese
     * @throws JSONException
     */
    private void editProfile() throws JSONException {
        String name = profileNameEditText.getText().toString();
        String base64Image = null;

        if (croppedProfilePicBitmap != null) {
            base64Image = Utils.getBase64FromBitmap(croppedProfilePicBitmap);
        }
        sendEditProfileRequest(name, base64Image);
    }

    /**
     * Fa la richiesta di rete per aggiornare nome dell'utente e/o immagine profilo
     * @throws JSONException
     */
    private void sendEditProfileRequest(String name, String base64Image) throws JSONException {
        cc = new CommunicationController(getContext());
        cc.setProfile(name, base64Image,
                response -> {
                    Log.d(TAG, "Richiesta di rete OK");
                    this.dismiss();
                    Snackbar snackbar = Snackbar
                            .make(getActivity().findViewById(R.id.bottomMenu), getResources().getString(R.string.updated_profile_info), Snackbar.LENGTH_LONG);
                    snackbar.setAnchorView(getActivity().findViewById(R.id.fab))
                            .show();
                    ((WallActivity) getContext()).getActualUserProfile();
                },
                error -> {
                    if (error.networkResponse.statusCode == 400) {
                        this.dismiss();
                        Snackbar snackbar = Snackbar
                                .make(getActivity().findViewById(R.id.bottomMenu),getResources().getString(R.string.already_used_username), Snackbar.LENGTH_LONG);
                        snackbar.setAnchorView(getActivity().findViewById(R.id.fab))
                                .show();
                    }
                    Log.e(TAG, "Errore richiesta: " + error);
                });
    }


    /**
     * Prende il bitmap dell'immagine, la fa diventare un quadrato (crop) e setta l'imageView del
     * profilo con essa
     * @param uri Uri dell'immagine di profilo
     */
    public void setProfilePicture(Uri uri) {
        Bitmap bitmap = Utils.getBitmapFromUri(uri, getContext().getContentResolver());
        croppedProfilePicBitmap = Utils.cropImageToSquare(bitmap);
        String base64Image = Utils.getBase64FromBitmap(croppedProfilePicBitmap);

        if (base64Image.length() > CommunicationController.MAX_IMAGE_LENGTH)  {
            this.dismiss();
            Snackbar snackbar = Snackbar
                    .make(getActivity().findViewById(R.id.bottomMenu),R.string.image_too_large_message, Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(getActivity().findViewById(R.id.fab))
                    .show();
        } else {
            editProfileButton.setEnabled(true);
            profilePictureImageView.setClipToOutline(true);
            profilePictureImageView.setImageBitmap(croppedProfilePicBitmap);
        }
    }
}