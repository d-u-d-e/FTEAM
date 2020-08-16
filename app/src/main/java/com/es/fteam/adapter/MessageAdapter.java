package com.es.fteam.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.es.fteam.ActivityLogin;
import com.es.fteam.fragments.FragmentChat;
import com.es.fteam.models.Message;
import com.es.fteam.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> chats;
    private static int MSG_TYPE_LEFT = 0;
    private static int MSG_TYPE_RIGHT = 1;
    private TextView newMessages = null;
    private int newMsgCount = 0;
    private int newMsgStartingPosition = 0;

    public MessageAdapter(Context c, List<Message> chats){
        context = c;
        this.chats = chats;
    }

    public void onNewMessagesRead(){
        if(newMessages != null){
            newMessages.setVisibility(View.GONE);
            newMessages = null;
        }
        newMsgCount = 0;
        newMsgStartingPosition = chats.size();
    }

    public void setNewMsgStartingPosition(int position){
        newMsgCount = chats.size() - position;
        newMsgStartingPosition = position;
    }

    public void incrementNewMessagesCounter(){
        newMsgCount++;

        if(newMessages == null){
            notifyItemChanged(newMsgStartingPosition);
        }
        else
            newMessages.setText(String.format(context.getString(R.string.new_unread_messages), newMsgCount));
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == MSG_TYPE_LEFT)
            view = LayoutInflater.from(context).inflate(R.layout.frag_chat_message_left, parent, false);
        else
            view = LayoutInflater.from(context).inflate(R.layout.frag_chat_message_right, parent, false);
        return new MessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        Message m = chats.get(position);
        holder.message.setText(m.getText());

        String dateStr = DateUtils.formatDateTime(context, m.getTimestamp(), DateUtils.FORMAT_ABBREV_MONTH |
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        holder.timestamp.setText(dateStr);
        holder.sender.setText(m.getSenderUsername());

        if(getItemCount() - 1 == position)
            FragmentChat.endReached = true;

        if(getItemViewType(position) == MSG_TYPE_LEFT){
            if(position == newMsgStartingPosition){
                newMessages = holder.newMessages;
                holder.newMessages.setVisibility(View.VISIBLE);
                holder.newMessages.setText(String.format(context.getString(R.string.new_unread_messages), newMsgCount));
            }
            else{
                holder.newMessages.setVisibility(View.GONE);
                holder.newMessages.setText("");
            }
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chats.get(position).getSenderID().equals(ActivityLogin.currentUserID))
            return MSG_TYPE_RIGHT;
        else
            return MSG_TYPE_LEFT;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView message;
        TextView timestamp;
        TextView sender;
        TextView newMessages;

        ViewHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chat_message);
            timestamp = itemView.findViewById(R.id.chat_timestamp);
            sender = itemView.findViewById(R.id.chat_user);
            newMessages = itemView.findViewById(R.id.chat_newMessages);
        }
    }
}
