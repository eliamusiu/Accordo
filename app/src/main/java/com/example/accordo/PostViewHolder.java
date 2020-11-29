package com.example.accordo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    private TextView  authorTextView, contentTextView;
    private ImageView contentImageView;
    private OnRecyclerViewClickListener recyclerViewClickListener;

    public PostViewHolder(@NonNull View itemView, OnRecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        this.recyclerViewClickListener = recyclerViewClickListener;
        authorTextView = itemView.findViewById(R.id.authorTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        contentImageView = itemView.findViewById(R.id.contentImageView);
        //itemView.setOnClickListener(this::onClick);
    }
    public void updateContent(Post post) {
        if (post.getName() != null) {
            authorTextView.setText(post.getName());
        }
        TextImagePost tiPost = (TextImagePost) post;
        contentTextView.setText(tiPost.getContent());
    }

    public void updateContent(TextImagePost post) {
        authorTextView.setText(post.getName());
        contentImageView.setImageBitmap(Utils.getBitmapFromBase64(post.getContent()));
        contentImageView.setOnClickListener(this::onClick);
    }

    public void updateContent(LocationPost post) {
        authorTextView.setText(post.getName());
    }

    public void onClick(View v) {
        recyclerViewClickListener.onRecyclerViewClick(v, getAdapterPosition());
    }
}
