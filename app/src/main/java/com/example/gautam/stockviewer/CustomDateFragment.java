package com.example.gautam.stockviewer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by Gautam on 6/22/2015.
 */
public class CustomDateFragment extends Fragment {
    final static String LOG_TAG = "Custom Date Fragment";

    private Button mBtn_start;
    private Button mBtn_end;
    private Button mBtn_search;

    private EditText mInputEText;
    private static String start_text;
    private static String end_text;
    private LineChart mChart;

    private static Calendar start_date = null;
    private static Calendar end_date = null;

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
        mBtn_search = (Button) rootView.findViewById(R.id.btn_search);
        mInputEText = (EditText) rootView.findViewById(R.id.input);
        start_date = Calendar.getInstance();
        end_date = Calendar.getInstance();
        Log.v(LOG_TAG, start_date.toString());
        Log.v((LOG_TAG), end_date.toString());
        mChart = (LineChart) rootView.findViewById(R.id.chart);
        Paint p = mChart.getPaint(Chart.PAINT_INFO);
        p.setColor(getResources().getColor(R.color.primary_dark_material_light));
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

        mBtn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mInputEText.getText().toString();
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (input.length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please input a stock first", Toast.LENGTH_SHORT).show();
                } else if (start_text == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please select a start date", Toast.LENGTH_SHORT).show();
                } else if (end_text == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please select an end date", Toast.LENGTH_SHORT).show();
                } else if (networkInfo != null && networkInfo.isConnected()) {
                    new getStockInfo().execute(input);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "You are not connected to the Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;

    }


    private class getStockInfo extends AsyncTask<String, Void, String> {
        final String baseUrl = "https://www.quandl.com/api/v1/datasets/WIKI/";
        final String APP_ID = "rNviB11yaDTGF8ycmsfY";
        final String ID_PARAM = "auth_token";
        final String cols = "column";
        final String start = "trim_start";
        final String end = "trim_end";
        final String numCol = "4";

//        final String LOG_TAG = this.getClass().getSimpleName();


        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection;
            String input = params[0];
            InputStream inputStream;
            String stockString = null;
            try {
                Uri builtUri = Uri.parse(baseUrl).buildUpon().appendPath(input + ".json").appendQueryParameter(ID_PARAM, APP_ID)
                        .appendQueryParameter(start, start_text).appendQueryParameter(end, end_text).appendQueryParameter(cols, numCol).build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                stockString = buffer.toString();
                Utility.getTodaysPrice(stockString);
                Utility.getDataRange(stockString);
            } catch (IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(), "Incorrect Ticker", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stockString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            if (Utility.getName() != null)
//                mNameTView.setText(Utility.getName());
//            if (Utility.getDate() != null)
//                mDateTView.setText(Utility.getDate());
//            if (Utility.getPrice() != null)
//                mOutputTView.setText(Double.toString(Utility.getPrice()));
            if (Utility.getLineData() != null) {
                mChart.setData(Utility.getLineData());
                mChart.setDescription("");
                mChart.invalidate(); // refresh
            }
        }


    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (pos == 0) {
                start_date = Calendar.getInstance();
                start_date.set(year, monthOfYear, dayOfMonth);
                start_text = start_date.get(Calendar.YEAR) + "-" + (start_date.get(Calendar.MONTH) + 1) + "-" + start_date.get(Calendar.DAY_OF_MONTH);
                Log.v(LOG_TAG, start_date.toString());
            } else if (pos == 1) {
                end_date = Calendar.getInstance();
                end_date.set(year, monthOfYear, dayOfMonth);
                end_text = end_date.get(Calendar.YEAR) + "-" + (end_date.get(Calendar.MONTH) + 1) + "-" + end_date.get(Calendar.DAY_OF_MONTH);
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
