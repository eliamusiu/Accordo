package com.example.accordo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

public class AddChannelFragment extends DialogFragment {
    final String TAG = String.valueOf(AddChannelFragment.class);

    public AddChannelFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Setta l'interfaccia
        builder.setTitle("Nuovo canale");
        builder.setView(R.layout.fragment_add_channel);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_channel, null);
        builder.setView(view);

        // Gestori eventi sui pulsanti del dialog
        builder.setPositiveButton("Aggiungi", (dialog, which) -> addChannel(view));
        builder.setNegativeButton("Annulla", (dialog, which) -> dismiss());

        return builder.create();
    }

    /**
     * Fa la richiesta di rete del {@link CommunicationController} prendendo il nome del canale
     * dalla EditText
     * @param view
     */
    private void addChannel(View view) {
        CommunicationController cc = new CommunicationController(getContext());
        final EditText et = view.findViewById(R.id.ctitleTextView);
        String ctitle = et.getText().toString();

        try {
            cc.addChannel(ctitle,
                    response -> {
                        Log.d(TAG, "Canale creato");
                        Snackbar snackbar = Snackbar
                                .make(getActivity().findViewById(R.id.bottomMenu),"Canale " + ctitle + " correttamente", Snackbar.LENGTH_LONG);
                        snackbar.setAnchorView(getActivity().findViewById(R.id.fab))
                                .show();
                    },
                    error -> Log.d(TAG, "Errore creazione canale: " + error.toString())
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dismiss();
    }
}