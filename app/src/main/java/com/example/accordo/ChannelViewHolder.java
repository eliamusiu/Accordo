package com.example.accordo;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChannelViewHolder extends RecyclerView.ViewHolder {
    private TextView channelFirstLetterTextView, myChannelsTextView, channelTitle, channelIndex;
    private OnRecyclerViewClickListener recyclerViewClickListener;
    private LinearLayout channelRowLinearLayout;

    public ChannelViewHolder(@NonNull View itemView, OnRecyclerViewClickListener recyclerViewClickListener) {
        super(itemView);
        channelFirstLetterTextView = itemView.findViewById(R.id.channelFirstLetterTextView);
        myChannelsTextView = itemView.findViewById(R.id.myChannelsTextView);
        channelTitle = itemView.findViewById(R.id.channelTitleTextView);
        channelIndex = itemView.findViewById(R.id.channelIndex);
        channelRowLinearLayout = itemView.findViewById(R.id.channelRowLinearLayout);
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    /**
     * Setta il testo di {@link #channelTitle} con il nome (titolo) del canale passato nella riga
     * della recyclerView e la mette bold se il canale è stato creato dall'utente loggato.
     * Setta anche la prima lettera (quella nel cerchio)
     * @param channel Canale del quale si vuole mostrare il nome
     */
    public void updateContent(Channel channel) {
        channelTitle.setText(channel.getCtitle());
        if (channel.getCtitle().equals("")) {
            channelFirstLetterTextView.setText("");
        } else {
            channelFirstLetterTextView.setText(Character.toString(channel.getCtitle().charAt(0)));
        }
        channelRowLinearLayout.setOnClickListener(this::onClick);
    }

    /**
     * Aggiorna l'indice dei canali (la lettera che raggruppa i canali con la stessa iniziale).
     * Se il canale è "mio" (dell'utente loggato) mette il drawable della stella al posto della
     * lettera
     * @param index
     * @param context
     */
    public void updateContent(String index, Context context) {
        if (index.equals(Channel.MY_CHANNEL_INDEX)) {
            channelIndex.setCompoundDrawablesWithIntrinsicBounds(context.getDrawable(R.drawable.ic_round_star_24), null, null, null);
            channelIndex.setPadding(Utils.getPixelsFromDp(16f, context), 0, 0, 0);
            channelIndex.setText("");
            myChannelsTextView.setVisibility(View.VISIBLE);
        } else {
            myChannelsTextView.setVisibility(View.GONE);
            channelIndex.setText(index);
        }
    }
    
    public void onClick(View v) {
        recyclerViewClickListener.onRecyclerViewClick(v, getAdapterPosition());
    }
}
