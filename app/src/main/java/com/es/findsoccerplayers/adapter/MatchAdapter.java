package com.es.findsoccerplayers.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.ActivityInfoBookedMatch;
import com.es.findsoccerplayers.models.Match;
import com.es.findsoccerplayers.R;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {

    private Context context;
    private List<Match> matches;

    public MatchAdapter(Context c, List<Match> matches){
        super();
        context = c;
        this.matches = matches;
    }

    @Override
    public MatchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.frag_match_item, parent, false);
        return new MatchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MatchAdapter.ViewHolder holder, int position) {
        Match m = matches.get(position);
        holder.item.setText(m.getDescription()); //TODO show better info
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        TextView item;

        ViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.frag_match_item);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    Match m = matches.get(position);
                    Intent i = new Intent(v.getContext(), ActivityInfoBookedMatch.class); //TODO
                    i.putExtra("match", m.getID());
                    v.getContext().startActivity(i);
                }
            });
        }
    }
}
