package com.example.accordo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileBottomSheetFragment extends BottomSheetDialogFragment {
    public static final String TAG = ProfileBottomSheetFragment.class.toString();
    private CommunicationController cc;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_bottom_sheet, container, false);

        try {
            cc = new CommunicationController(getContext());
            cc.getProfile(response -> {
                        setInterfaceProfileInfo(response, view);
                    },
                    error -> Log.e(TAG, "Errore richiesta: " + error));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.findViewById(R.id.editProfileButton).setOnClickListener(v -> {
            try {
                editProfile(view);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return view;
    }

    private void setInterfaceProfileInfo(JSONObject response, View view) {
        try {
            Object nameObject = response.get("name");
            if (!nameObject.equals(null)) {
                String name = (String) nameObject;
                ((EditText)view.findViewById(R.id.profileNameEditText)).setText(name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void editProfile(View view) throws JSONException {
        String name = ((EditText) view.findViewById(R.id.profileNameEditText)).getText().toString();
        cc = new CommunicationController(getContext());
        cc.setProfile(name, null,
                response -> Log.d(TAG, "Richiesta di rete OK"),
                error -> Log.e(TAG, "Errore richiesta: " + error));
    }
}