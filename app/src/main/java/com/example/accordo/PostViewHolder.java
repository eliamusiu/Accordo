package com.example.accordo;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    private TextView  authorTextView, contentTextView;
    private ImageView contentImageView, profileImageView;
    private LinearLayout postContainerLinearLayout, postLinearLayout, authorLinearLayout;
    private Button locationLinearLayout;
    private OnPostRecyclerViewClickListener recyclerViewClickListener;
    private Context context;

    public PostViewHolder(@NonNull View itemView, Context context, OnPostRecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        this.recyclerViewClickListener = recyclerViewClickListener;
        this.context = context;
        postContainerLinearLayout = itemView.findViewById(R.id.postContainerLinearLayout);
        postLinearLayout = itemView.findViewById(R.id.postLinearLayout);
        authorLinearLayout = itemView.findViewById(R.id.authorLinearLayout);
        authorTextView = itemView.findViewById(R.id.authorTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
        contentImageView = itemView.findViewById(R.id.contentImageView);
        locationLinearLayout = itemView.findViewById(R.id.locationPostButton);
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
            contentImageView.setClipToOutline(true);
            contentImageView.setImageBitmap(Utils.getBitmapFromBase64(post.getContent(), context));
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
     * Allinea a destra i post pubblicati dall'utente attualmente loggato, nasconde il suo nome
     * e la sua immagine di profilo, mette il layout "fumetto" di invio e il colorOnPrimary per il
     * testo (se è un post di tipo testo)
     */
    public void setActualUserStyleForPost() {
        postContainerLinearLayout.setGravity(Gravity.RIGHT);
        authorLinearLayout.setVisibility(View.GONE);
        postLinearLayout.setBackgroundResource(R.drawable.my_post_background);
        if (contentTextView != null) {
            contentTextView.setTextColor(Utils.getThemeAttr(R.attr.colorOnPrimary, context));
        }
    }

    /**
     * Allinea a destra i post pubblicati dagli altri utenti, rende visibile il nome dell'utente
     * e la sua immagine di profilo, mette il layout "fumetto" di ricezione e il colorOnBackground
     * per il testo (se è un post di tipo testo)
     */
    public void setOtherUsersStyleForPost() {
        postContainerLinearLayout.setGravity(Gravity.LEFT);
        authorLinearLayout.setVisibility(View.VISIBLE);
        postLinearLayout.setBackgroundResource(R.drawable.their_post_background);
        if (contentTextView != null) {
            contentTextView.setTextColor(Utils.getThemeAttr(R.attr.colorOnBackground, context));
        }
    }

    /**
     * Setta l'{@link ImageView} dell'immagine del profilo dell'autore
     * @param postAuthor Autore del post dal prendere l'immagine per settare l'{@link ImageView}
     */
    public void setProfilePicture(User postAuthor) {
        if (postAuthor == null || postAuthor.getPicture().equals("null")) {     // TODO: vedi italiantitan su canale 1
            profileImageView.setImageResource(R.drawable.ic_round_account_circle_24);
        } else {
            profileImageView.setImageBitmap(Utils.getBitmapFromBase64(postAuthor.getPicture(), context));
        }
    }

    public void onImageClick(View v) {
        recyclerViewClickListener.onRecyclerViewImageClick(v, getAdapterPosition());
    }

    public void onLocationClick(View v) {
        recyclerViewClickListener.onRecyclerViewLocationClick(v, getAdapterPosition());
    }
}
