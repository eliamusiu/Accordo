package com.example.accordo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class PopupAttach {

    public void showPopupWindow(final View view, View newPostView, Context context) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.attach_popup, null);

        // Specifica la larghezza e l'altezza mediante costanti
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;       // Rende inattivi gli elementi al di fuori del popup

        //Create a window with our parameters
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(Utils.getPixelsFromDp(16f, context));

        // Imposta la posizione del popup sullo schermo
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int marginX = Utils.getPixelsFromDp(8f, context);
        int marginY = Utils.getPixelsFromDp(16f, context);
        popupWindow.showAsDropDown(newPostView, -popupView.getMeasuredWidth() - marginX,
                -newPostView.getHeight() - popupView.getMeasuredHeight() - marginY);

        // Gestore evento di click su bottone allega immagine
        popupView.findViewById(R.id.attachImageButton).setOnClickListener(v -> {
            popupWindow.dismiss();
            ((ChannelActivity)context).onAttachClick("i");
        });

        // Gestore evento di click su bottone allega posizione
        popupView.findViewById(R.id.attachLocationButton).setOnClickListener(v -> {
            popupWindow.dismiss();
            ((ChannelActivity)context).onAttachClick("l");
        });
    }
}
