package com.example.accordo;

import android.view.View;

public interface OnPostRecyclerViewClickListener {
    void onRecyclerViewImageClick(View v, int position);
    void onRecyclerViewLocationClick(View v, int position);
}
