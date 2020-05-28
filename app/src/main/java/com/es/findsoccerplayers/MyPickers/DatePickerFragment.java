package com.es.findsoccerplayers.MyPickers;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.es.findsoccerplayers.R;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static String date;



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



    //Quando l'utente inserisce la data scelta, passa le informazioni al metodo
    // setTheDate di MatchActivity
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        TextView matchDate = getActivity().findViewById(R.id.date_text);
        matchDate.setText(String.format("%02d / %02d / %04d", dayOfMonth, month + 1, year));
    }


}