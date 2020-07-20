package com.es.findsoccerplayers.pickers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

public class NumberPickerFragment extends DialogFragment {

    public interface OnCompleteListener {
        void onNumberSet(int number);
    }

    private OnCompleteListener mListener;
    private Context context;
    private String title;

    public NumberPickerFragment(Context c, NumberPickerFragment.OnCompleteListener listener, String title){
        mListener = listener;
        context = c;
        this.title = title;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final NumberPicker np = new NumberPicker(context);
        np.setMaxValue(20);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        np.setValue(1);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title).setView(np).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onNumberSet(np.getValue());
            }
        });
        return alertDialog.create();
    }
}
