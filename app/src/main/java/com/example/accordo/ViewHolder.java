package com.example.accordo;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class ViewHolder extends RecyclerView.ViewHolder {
    private TextView channelTitle, authorTextView, contentTextView;
    private OnRecyclerViewClickListener recyclerViewClickListener;

    public ViewHolder(@NonNull View itemView, OnRecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        itemView.setOnClickListener(this::onClick);
        channelTitle = itemView.findViewById(R.id.channelTitle);
        this.recyclerViewClickListener = recyclerViewClickListener;
        authorTextView = itemView.findViewById(R.id.authorTextView);
        contentTextView = itemView.findViewById(R.id.contentTextView);
    }

    public void updateContent(Channel channel) {
        channelTitle.setText(channel.getCtitle());
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
    }

    public void updateContent(LocationPost post) {
        authorTextView.setText(post.getName());
    }

    public void onClick(View v) {
        recyclerViewClickListener.onRecyclerViewClick(v, getAdapterPosition());
    }
}
