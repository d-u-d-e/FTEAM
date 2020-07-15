package com.es.findsoccerplayers.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.models.SettingsElement;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {
    private List<SettingsElement> settingsElementList;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener=listener;
    }

    public static class SettingsViewHolder extends RecyclerView.ViewHolder {
        public ImageView im;
        public TextView tx1, tx2;

        public SettingsViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            im = itemView.findViewById(R.id.settings_element_img);
            tx1 = itemView.findViewById(R.id.settings_element_text1);
            tx2 = itemView.findViewById(R.id.settings_element_text2);

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

    public SettingsAdapter(List<SettingsElement> settingsElementList){
        this.settingsElementList = settingsElementList;
    }

    @Override
    public SettingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_element, parent, false);
        return new SettingsViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(SettingsViewHolder holder, int position) {
        SettingsElement item = settingsElementList.get(position);
        holder.im.setImageResource(item.getImage());
        holder.tx1.setText(item.getText1());
        holder.tx2.setText(item.getText2());
    }

    @Override
    public int getItemCount() {
        return settingsElementList.size();
    }

}
