package com.es.findsoccerplayers.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.ActivityInfoBookedMatch;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.models.Match;
import com.es.findsoccerplayers.R;

import java.util.Calendar;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder>{
    private Context context;
    private List<Match> matches;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener=listener;
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder{
        public TextView day, month, field, hour, desc;

        public MatchViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            day = itemView.findViewById(R.id.match_element_gg);
            month = itemView.findViewById(R.id.match_element_mm);
            field = itemView.findViewById(R.id.match_element_field);
            hour = itemView.findViewById(R.id.match_element_time);
            desc = itemView.findViewById(R.id.match_element_desc);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public MatchAdapter(List<Match> matches){
        this.matches = matches;
    }

    @Override
    public MatchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_match_item, parent, false);
        return new MatchViewHolder(view,listener);
    }


    @Override
    public void onBindViewHolder(MatchViewHolder holder, int position) {
        Match m = matches.get(position);
        holder.day.setText(" "+Utils.extractDay(m.getMatchData()));
        holder.month.setText(" "+Utils.extractMonth(m.getMatchData()));
        holder.field.setText(" "+m.getPlaceName());
        holder.hour.setText(" "+m.getMatchHour());
        holder.desc.setText(" "+Utils.getPreviewDescription(m.getDescription()));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }


}
