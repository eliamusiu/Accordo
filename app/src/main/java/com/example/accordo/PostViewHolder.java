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
    private ImageView contentImageView, profileImageView;
    private LinearLayout locationLinearLayout;
    private OnPostRecyclerViewClickListener recyclerViewClickListener;


    public PostViewHolder(@NonNull View itemView, OnPostRecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        this.recyclerViewClickListener = recyclerViewClickListener;
        authorTextView = itemView.findViewById(R.id.authorTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        contentImageView = itemView.findViewById(R.id.contentImageView);
        locationLinearLayout = itemView.findViewById(R.id.locationLinearLayout);
        profileImageView = itemView.findViewById(R.id.profileImageView);
    }

    /* Post tipo testo */
    public void updateContent(Post post, String picture) {
        setUserInfo(post, picture);
        TextImagePost tiPost = (TextImagePost) post;
        contentTextView.setText(tiPost.getContent());
    }

    /* Post tipo immagine */
    public void updateContent(TextImagePost post, String picture) {
        setUserInfo(post, picture);
        if (post.getContent() != null) {
            contentImageView.setImageBitmap(Utils.getBitmapFromBase64(post.getContent()));
            contentImageView.setOnClickListener(this::onImageClick);
        }
    }

    /* Post tipo posizione */
    public void updateContent(LocationPost post, String picture) {
        setUserInfo(post, picture);
        locationLinearLayout.setOnClickListener(this::onLocationClick);
    }

    private void setUserInfo(Post post, String picture) {
        if (post.getName() != null) {                   // Se c'è il nome dell'autore lo setta
            authorTextView.setText(post.getName());
        }
        if (picture != null) {                          // Se c'è l'immagine di profilo la setta
            profileImageView.setImageBitmap(Utils.getBitmapFromBase64(picture));
        }
    }

    public void onImageClick(View v) {
        recyclerViewClickListener.onRecyclerViewImageClick(v, getAdapterPosition());
    }

    public void onLocationClick(View v) {
        recyclerViewClickListener.onRecyclerViewLocationClick(v, getAdapterPosition());
    }
}
