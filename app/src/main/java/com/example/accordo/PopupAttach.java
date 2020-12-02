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
        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.attach_popup, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.showAsDropDown(newPostView, 0, -newPostView.getHeight() - popupView.getMeasuredHeight(), Gravity.RIGHT);

        popupWindow.setAnimationStyle(android.R.anim.fade_in);

        //Handler for clicking
        popupView.findViewById(R.id.attachImageButton).setOnClickListener(v -> {
            popupWindow.dismiss();
            ((ChannelActivity)context).onClick("i");
        });

        popupView.findViewById(R.id.attachLocationButton).setOnClickListener(v -> {
            popupWindow.dismiss();
            ((ChannelActivity)context).onClick("l");
        });
    }
}
