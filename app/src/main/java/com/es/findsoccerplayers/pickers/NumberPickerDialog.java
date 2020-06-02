package com.es.findsoccerplayers.pickers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.es.findsoccerplayers.R;


public class NumberPickerDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final NumberPicker np = new NumberPicker(getActivity());
        np.setMaxValue(20);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        np.setValue(1);


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.how_many_players).setView(np).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int i = np.getValue();
                //Take a reference to the TextView ad set the text
                TextView numberPlayer = getActivity().findViewById(R.id.cr_match_playersNumber);
                numberPlayer.setText(String.valueOf(i));
            }
        });


        return alertDialog.create();

    }

}
