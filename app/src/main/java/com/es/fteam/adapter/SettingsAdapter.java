package com.es.fteam.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.es.fteam.R;
import com.es.fteam.models.SettingsElement;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {
    private List<SettingsElement> settingsElementList;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener=listener;
    }

    static class SettingsViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView im;
        TextView tx1, tx2;

        SettingsViewHolder(View itemView, final OnItemClickListener listener) {
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

    public SettingsAdapter(Context context, List<SettingsElement> settingsElementList){
        this.settingsElementList = settingsElementList;
        this.context = context;
    }

    @Override
    public SettingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_element, parent, false);
        return new SettingsViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(SettingsViewHolder holder, int position) {
        SettingsElement item = settingsElementList.get(position);
        Drawable drawable = AppCompatResources.getDrawable(context, item.getImage());
        holder.im.setImageDrawable(drawable);
        holder.tx1.setText(item.getText());
        holder.tx2.setText(item.getTextDetailed());
    }

    @Override
    public int getItemCount() {
        return settingsElementList.size();
    }

}
