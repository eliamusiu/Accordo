package com.example.accordo;

import android.graphics.Typeface;
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

    /**
     * Setta il testo di {@link #channelTitle} con il nome (titolo) del canale passato nella riga
     * della recyclerView e la mette bold se il canale è stato creato dall'utente loggato
     * @param channel Canale del quale si vuole mostrare il nome
     */
    public void updateContent(Channel channel) {
        channelTitle.setText(channel.getCtitle());
        /*
        if (channel.getMine().equals("t")) {
            channelTitle.setTypeface(null, Typeface.BOLD);
        }*/
    }
    
    public void onClick(View v) {
        recyclerViewClickListener.onRecyclerViewClick(v, getAdapterPosition());
    }
}
