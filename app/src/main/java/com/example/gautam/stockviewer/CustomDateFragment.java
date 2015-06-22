package com.example.gautam.stockviewer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by Gautam on 6/22/2015.
 */
public class CustomDateFragment extends Fragment {
    final static String LOG_TAG = "Custom Date Fragment";

    private Button mBtn_start;
    private Button mBtn_end;
    private static TextView mStart_date;
    private static TextView mEnd_date;


    private static Calendar start_date;
    private static Calendar end_date;

    private static int pos;

    public CustomDateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_custom_date, container,
                false);
        mBtn_start = (Button) rootView.findViewById(R.id.btn_start);
        mBtn_end = (Button) rootView.findViewById(R.id.btn_end);
        mStart_date = (TextView) rootView.findViewById(R.id.date_start);
        mEnd_date = (TextView) rootView.findViewById(R.id.date_end);
        start_date = Calendar.getInstance();
        end_date = Calendar.getInstance();
        mBtn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
                pos = 0;

            }
        });

        mBtn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
                pos = 1;

            }
        });
        return rootView;

    }


    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (pos == 0) {
                start_date = Calendar.getInstance();
                start_date.set(year, monthOfYear, dayOfMonth);
                mStart_date.setText(start_date.get(Calendar.YEAR) + "-" + (start_date.get(Calendar.MONTH)+1) + "-" + start_date.get(Calendar.DAY_OF_MONTH));
                Log.v(LOG_TAG, start_date.toString());
            } else if (pos == 1) {
                end_date = Calendar.getInstance();
                end_date.set(year, monthOfYear, dayOfMonth);
                mEnd_date.setText(end_date.get(Calendar.YEAR) + "-" + (end_date.get(Calendar.MONTH)+1) + "-" + end_date.get(Calendar.DAY_OF_MONTH));
                Log.v(LOG_TAG, end_date.toString());
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

    }


}
