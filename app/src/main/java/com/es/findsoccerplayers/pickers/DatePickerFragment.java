package com.es.findsoccerplayers.pickers;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public interface OnCompleteListener {
        void onDateSet(int year, int month, int dayOfMonth);
    }

    private OnCompleteListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it with the today date
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mListener = (OnCompleteListener)context;
    }

    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mListener.onDateSet(year, month, dayOfMonth);
    }
}