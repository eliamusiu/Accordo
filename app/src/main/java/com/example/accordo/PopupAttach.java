package com.example.accordo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
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
        Rect location = locateView(newPostView);
        int yOffset = getScreenHeight(context) - location.top;
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
        popupWindow.showAtLocation(view, Gravity.BOTTOM|Gravity.RIGHT, 0, yOffset + newPostView.getHeight());
        popupWindow.setAnimationStyle(android.R.anim.fade_in);

        //Initialize the elements of our window, install the handler

        //Handler for clicking
        popupView.findViewById(R.id.attachImageButton).setOnClickListener(v -> {
            popupWindow.dismiss();
            ((ChannelActivity)context).onClick();
        });
    }

    public static Rect locateView(View v)
    {
        int[] loc_int = new int[2];
        v.getLocationOnScreen(loc_int);
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    private static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        return metrics.heightPixels;
    }
}
