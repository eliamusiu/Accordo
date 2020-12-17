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
    private static final int CHANNEL_TYPE = 1, INDEX_TYPE = 0;

    public ChannelAdapter(Context context, OnRecyclerViewClickListener recyclerViewClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.recyclerViewClickListener = recyclerViewClickListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == INDEX_TYPE) {
            View view = inflater.inflate(R.layout.channel_index_list_row, parent, false);
            return new ChannelViewHolder(view, recyclerViewClickListener);
        } else {
            View view = inflater.inflate(R.layout.channel_list_row, parent, false);
            return new ChannelViewHolder(view, recyclerViewClickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChannelViewHolder viewHolder = (ChannelViewHolder) holder;
        if (getItemViewType(position) == INDEX_TYPE) {
            viewHolder.updateContent(Model.getInstance(context).getChannel(position).getIndex(), context);
        } else {
            viewHolder.updateContent(Model.getInstance(context).getChannel(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (Model.getInstance(context).getChannel(position).getIndex() != null) {
            return INDEX_TYPE;
        } else {
            return CHANNEL_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return Model.getInstance(context).getChannelsSize();
    }

}