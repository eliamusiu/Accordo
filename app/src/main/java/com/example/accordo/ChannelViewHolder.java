package com.example.accordo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class ChannelViewHolder extends RecyclerView.ViewHolder {
    private TextView channelTitle;
    private OnRecyclerViewClickListener recyclerViewClickListener;

    public ChannelViewHolder(@NonNull View itemView, OnRecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        itemView.setOnClickListener(this::onClick);
        channelTitle = itemView.findViewById(R.id.channelTitle);
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    public void updateContent(Channel channel) {
        channelTitle.setText(channel.getCtitle());
    }

    public void onClick(View v) {
        recyclerViewClickListener.onRecyclerViewClick(v, getAdapterPosition());
    }
}
