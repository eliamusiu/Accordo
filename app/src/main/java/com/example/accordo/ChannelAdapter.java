package com.example.accordo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater inflater;
    private OnRecyclerViewClickListener recyclerViewClickListener;
    private Context context;

    public ChannelAdapter(Context context, OnRecyclerViewClickListener recyclerViewClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.recyclerViewClickListener = recyclerViewClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.channel_list_row, parent, false);
        return new ChannelViewHolder(view, recyclerViewClickListener);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChannelViewHolder viewHolder = (ChannelViewHolder) holder;
        viewHolder.updateContent(Model.getInstance(context).getChannel(position));

    }

    @Override
    public int getItemCount() {
        return Model.getInstance(context).getChannelsSize();
    }

}