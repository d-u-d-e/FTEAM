package com.es.fteam.pickers;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    public interface OnCompleteListener {
        void onTimeSet(int hour, int minute);
    }

    private OnCompleteListener mListener;
    private Context context;

    public TimePickerFragment(Context c, TimePickerFragment.OnCompleteListener listener){
        mListener = listener;
        context = c;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(context, this, hour, minute,
                true);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mListener.onTimeSet(hourOfDay, minute);
    }
}
