package com.example.accordo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater inflater;
    private OnPostRecyclerViewClickListener recyclerViewClickListener;
    private Context context;

    public PostAdapter(Context context, OnPostRecyclerViewClickListener recyclerViewClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.recyclerViewClickListener = recyclerViewClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1:
                View tView = inflater.inflate(R.layout.text_post_list_row, parent, false);
                return new PostViewHolder(tView, recyclerViewClickListener);
            case 2:
                View iView = inflater.inflate(R.layout.image_post_list_row, parent, false);
                return new PostViewHolder(iView, recyclerViewClickListener);
            case 3:
                View lView = inflater.inflate(R.layout.location_post_list_row, parent, false);
                return new PostViewHolder(lView, recyclerViewClickListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Post post = Model.getInstance(context).getPost(position);
        User postAuthor = Model.getInstance(context).getUser(post.getUid());
        String picture = null;
        if (postAuthor != null) {
            picture = postAuthor.getPicture();
        }
        PostViewHolder viewHolder = (PostViewHolder) holder;
        switch (holder.getItemViewType()) {
            case 1:
                viewHolder.updateContent(post, picture);
                break;
            case 2:
                viewHolder.updateContent((TextImagePost) post, picture);
                break;
            case 3:
                viewHolder.updateContent((LocationPost) post, picture);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Post post = Model.getInstance(context).getPost(position);
        switch (post.getType()) {
            case "t":
                return 1;
            case "i":
                return 2;
            case "l":
                return 3;
        }
        return 0;
    }

    public void notifyData() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return Model.getInstance(context).getPostsSize();
    }
}
