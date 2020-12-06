package com.example.accordo;

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

    /**
     * Aggiorna le view dei post di tipo testo
     * @param post Post che si vuole mostrare nella riga della recyclerView
     */
    public void updateContent(Post post) {
        TextImagePost tiPost = (TextImagePost) post;
        contentTextView.setText(tiPost.getContent());
    }

    /**
     * Aggiorna le view dei post di tipo immagine
     * @param post Post che si vuole mostrare nella riga della recyclerView
     */
    public void updateContent(TextImagePost post) {
        if (post.getContent() != null) {
            contentImageView.setImageBitmap(Utils.getBitmapFromBase64(post.getContent()));
            contentImageView.setOnClickListener(this::onImageClick);
        }
    }

    /**
     * Setta il click listener del {@link LinearLayout} del post di tipo posizione
     */
    public void updateContent() {
        locationLinearLayout.setOnClickListener(this::onLocationClick);
    }

    /**
     * Aggiorna la {@link TextView} del nome dell'autore con quello passato e, se null, imposta
     * il nome di default ("autore sconosciuto")
     * @param name Nome dell'autore (o null, se non c'è)
     */
    public void setUserName(String name) {
        if (name != null) {                   // Se c'è il nome dell'autore lo setta
            authorTextView.setText(name);
        } else {
            authorTextView.setText(R.string.unknown_author);
        }
    }

    /**
     * Setta l'{@link ImageView} dell'immagine del profilo dell'autore
     * @param picture Immagine base64 da settare
     */
    public void setProfilePicture(String picture) {
        profileImageView.setImageBitmap(Utils.getBitmapFromBase64(picture));
    }

    public void onImageClick(View v) {
        recyclerViewClickListener.onRecyclerViewImageClick(v, getAdapterPosition());
    }

    public void onLocationClick(View v) {
        recyclerViewClickListener.onRecyclerViewLocationClick(v, getAdapterPosition());
    }
}
