package com.example.accordo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    private TextView channelTitle;
    private OnRecyclerViewClickListener recyclerViewClickListener;

    public ViewHolder(@NonNull View itemView, OnRecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        itemView.setOnClickListener(this::onClick);
        channelTitle = itemView.findViewById(R.id.channelTitle);
        this.recyclerViewClickListener = recyclerViewClickListener;

    }

    public void updateContent(Channel channel) {
        channelTitle.setText(channel.getCtitle());
    }

    public void updateContent(Post post) {
        channelTitle.setText(post.getName());
    }

    public void onClick(View v) {
        recyclerViewClickListener.onRecyclerViewClick(v, getAdapterPosition());
    }
}
