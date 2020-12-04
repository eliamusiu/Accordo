package com.example.accordo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    private TextView  authorTextView, contentTextView;
    private ImageView contentImageView;
    private LinearLayout locationLinearLayout;
    private OnPostRecyclerViewClickListener recyclerViewClickListener;


    public PostViewHolder(@NonNull View itemView, OnPostRecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        this.recyclerViewClickListener = recyclerViewClickListener;
        authorTextView = itemView.findViewById(R.id.authorTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        contentImageView = itemView.findViewById(R.id.contentImageView);
        locationLinearLayout = itemView.findViewById(R.id.locationLinearLayout);
    }
    public void updateContent(Post post) {
        if (post.getName() != null) {
            authorTextView.setText(post.getName());
        }
        TextImagePost tiPost = (TextImagePost) post;
        contentTextView.setText(tiPost.getContent());
    }

    public void updateContent(TextImagePost post) {
        if (post.getName() != null) {
            authorTextView.setText(post.getName());
        }
        contentImageView.setImageBitmap(Utils.getBitmapFromBase64(post.getContent()));
        contentImageView.setOnClickListener(this::onImageClick);
    }

    public void updateContent(LocationPost post) {
        if (post.getName() != null) {
            authorTextView.setText(post.getName());
        }
        locationLinearLayout.setOnClickListener(this::onLocationClick);
    }

    public void onImageClick(View v) {
        recyclerViewClickListener.onRecyclerViewImageClick(v, getAdapterPosition());
    }

    public void onLocationClick(View v) {
        recyclerViewClickListener.onRecyclerViewLocationClick(v, getAdapterPosition());
    }
}
