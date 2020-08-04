package com.es.findsoccerplayers.dialogues;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.es.findsoccerplayers.R;

public class EditDescriptionDialogue extends DialogFragment {

    public interface onDescriptionListener {
        void onDescriptionSet(String desc);
    }

    private EditText editText;
    private onDescriptionListener mListener;
    private String prevDesc;

    public EditDescriptionDialogue(onDescriptionListener listener, String prevDesc){
        mListener = listener;
        this.prevDesc = prevDesc;
    }

    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_description_dialogue, null);
        editText = view.findViewById(R.id.ET_description);
        editText.setText(prevDesc);

        builder.setView(view).setTitle("Insert a description")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDescriptionSet(editText.getText().toString());
                    }
                });

        return builder.create();
    }
}