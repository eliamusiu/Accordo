package com.example.accordo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater inflater;
    private OnRecyclerViewClickListener recyclerViewClickListener;
    private ArrayList<T> items;
    private String holderType;

    public Adapter(Context context, ArrayList<T> items, String holderType, OnRecyclerViewClickListener recyclerViewClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.recyclerViewClickListener = recyclerViewClickListener;
        this.items = items;
        this.holderType = holderType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (holderType == "channels") {
            View view = inflater.inflate(R.layout.channel_list_row, parent, false);
            return new ViewHolder(view, recyclerViewClickListener);
        } else if (holderType == "posts") {
            switch (viewType) {
                case 0:
                    View view = inflater.inflate(R.layout.text_post_list_row, parent, false);
                    return new ViewHolder(view, recyclerViewClickListener);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holderType == "channels") {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.updateContent((Channel) items.get(position));
        } else if (holderType == "posts") {
            switch (holder.getItemViewType()) {
                case 0:
                    ViewHolder viewHolder = (ViewHolder) holder;
                    viewHolder.updateContent((Channel) items.get(position));
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (holderType == "posts") {
            Post post = (Post) items.get(position);
            switch (post.getType()) {
                case "t":
                    return 0;
                case "i":
                    return 1;
                case "l":
                    return 2;
            }
        }
        return 0;
    }
}