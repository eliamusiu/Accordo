package com.example.accordo;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater inflater;
    private OnPostRecyclerViewClickListener recyclerViewClickListener;
    private Context context;
    private static final int POST_TYPE_TEXT = 1, POST_TYPE_IMAGE = 2, POST_TYPE_LOCATION = 3;

    public PostAdapter(Context context, OnPostRecyclerViewClickListener recyclerViewClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.recyclerViewClickListener = recyclerViewClickListener;
        this.context = context;
    }

    /**
     * In base al ViewType impostato in {@link #getItemViewType(int)} imposta nel
     * {@link PostViewHolder} il tipo di layout che il quel post deve avere
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case POST_TYPE_TEXT:
                View tView = inflater.inflate(R.layout.text_post_list_row, parent, false);
                return new PostViewHolder(tView, context, recyclerViewClickListener);
            case POST_TYPE_IMAGE:
                View iView = inflater.inflate(R.layout.image_post_list_row, parent, false);
                return new PostViewHolder(iView, context, recyclerViewClickListener);
            case POST_TYPE_LOCATION:
                View lView = inflater.inflate(R.layout.location_post_list_row, parent, false);
                return new PostViewHolder(lView, context, recyclerViewClickListener);
        }
        return null;
    }

    /**
     * Ottiene da {@link Model#getPost(int)} il {@link Post} e da {@link Model#getUser(String)}
     * lo {@link User} per prendere la sua immagine di profilo da passare al {@link PostViewHolder}.
     * Chiama {@link PostViewHolder#setUserName(String)},
     * {@link #updateViewHolder(PostViewHolder, Post)},
     * {@link PostViewHolder#setProfilePicture(User)} e
     * {@link PostViewHolder#setActualUserStyleForPost()}
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Post post = Model.getInstance(context).getPost(position);
        User postAuthor = Model.getInstance(context).getUser(post.getUid());
        PostViewHolder viewHolder = (PostViewHolder)holder;

        viewHolder.setUserName(post.getName());     // Setta il nome utente a ogni tipo di post (i, l, t)
        updateViewHolder(viewHolder, post);         // Aggiorna il contenuto in base al tipo di post
        viewHolder.setProfilePicture(postAuthor);   // Setta l'immagine di profilo per ogni post
        if (Model.getInstance(context).getActualUser().getUid().equals(post.getUid())) {
            viewHolder.setActualUserStyleForPost();
        } else {
            viewHolder.setOtherUsersStyleForPost();
        }
    }

    /**
     * In base al ViewType ritornato in {@link #getItemViewType(int)} chiama un metodo del
     * {@link PostViewHolder} diverso per tipo di post.
     * @param viewHolder
     * @param post
     */
    private void updateViewHolder(PostViewHolder viewHolder, Post post) {
        switch (viewHolder.getItemViewType()) {
            case POST_TYPE_TEXT:
                viewHolder.updateContent(post);
                break;
            case POST_TYPE_IMAGE:
                viewHolder.updateContent((TextImagePost) post);
                break;
            case POST_TYPE_LOCATION:
                viewHolder.updateContent();
                break;
        }
    }

    /**
     * In base al tipo di post alla posizione passata ritorna una costante numerica diversa
     * in modo da identificare il tipo di post direttamente negli utilizzi successivi
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        Post post = Model.getInstance(context).getPost(position);
        switch (post.getType()) {
            case Post.TEXT:
                return POST_TYPE_TEXT;
            case Post.IMAGE:
                return POST_TYPE_IMAGE;
            case Post.LOCATION:
                return POST_TYPE_LOCATION;
        }
        return 0;
    }

    /**
     * Aggiorna la recyclerView con i nuovi aggiornamenti del {@link Model}
     */
    public void notifyData() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return Model.getInstance(context).getPostsSize();
    }
}
