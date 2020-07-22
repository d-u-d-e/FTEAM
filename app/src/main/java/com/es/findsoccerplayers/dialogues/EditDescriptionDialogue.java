package com.es.findsoccerplayers.dialogue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.es.findsoccerplayers.R;

public class EditDescriptionDialogue extends DialogFragment {

    public interface onDescriptionListener {
        void onDescriptionSet(String desc);
    }

    private EditText ETdesc;
    private onDescriptionListener mListener;
    private Context context;
    private String title, prevDesc;

    public EditDescriptionDialogue(Context c, EditDescriptionDialogue.onDescriptionListener listener, String title, String prevDesc){
        mListener = listener;
        context = c;
        this.title = title;
        this.prevDesc = prevDesc;
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_description_dialogue, null);
        ETdesc = view.findViewById(R.id.ET_description);
        ETdesc.setText(prevDesc);
        Log.w("DIALOGUE DESC", prevDesc);

        builder.setView(view).setTitle("Insert a description")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDescriptionSet(ETdesc.getText().toString());
                    }
                });

        return builder.create();
    }

}