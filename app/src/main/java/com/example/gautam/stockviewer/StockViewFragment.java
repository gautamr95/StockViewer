package com.example.gautam.stockviewer;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StockViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StockViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StockViewFragment extends Fragment {
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    final String LOG_TAG = this.getClass().getSimpleName();

    private Button mBtn;
    private EditText mInputEText;
    private EditText mNumberEText;
    private TextView mOutputTView;
    private TextView mDateTView;
    private TextView mNameTView;
    private LineChart mChart;

//    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StockViewFragment.
     */
//    // TODO: Rename and change types and number of parameters
//    public static StockViewFragment newInstance(String param1, String param2) {
//        StockViewFragment fragment = new StockViewFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
    public StockViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock_view, container,
                false);
        mBtn = (Button) rootView.findViewById(R.id.search_btn);
        mInputEText = (EditText) rootView.findViewById(R.id.input);
        mNameTView = (TextView) rootView.findViewById(R.id.name);
        mOutputTView = (TextView) rootView.findViewById(R.id.price_today);
        mDateTView = (TextView) rootView.findViewById(R.id.date_today);
        mChart = (LineChart) rootView.findViewById(R.id.chart);
        mNumberEText = (EditText) rootView.findViewById(R.id.number);

        Paint p = mChart.getPaint(Chart.PAINT_INFO);
        p.setColor(getResources().getColor(R.color.primary_dark_material_light));
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mInputEText.getText().toString();
                String number = mNumberEText.getText().toString();
                ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (input.length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please input a stock first", Toast.LENGTH_SHORT).show();
                } else if (number.length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please input the number of days", Toast.LENGTH_SHORT).show();
                } else if (networkInfo != null && networkInfo.isConnected()) {
                    new getStockInfo().execute(input, number);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "You are not connected to the Internet", Toast.LENGTH_SHORT).show();
                }
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mInputEText.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(mNumberEText.getWindowToken(), 0);
            }
        });
        return rootView;
    }


    private class getStockInfo extends AsyncTask<String, Void, String> {
        final String baseUrl = "https://www.quandl.com/api/v1/datasets/WIKI/";
        final String APP_ID = "rNviB11yaDTGF8ycmsfY";
        final String ID_PARAM = "auth_token";
        final String rows = "rows";
        final String cols = "column";
        String numRow = "1";
        final String numCol = "4";

//        final String LOG_TAG = this.getClass().getSimpleName();


        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection;
            String input = params[0];
            numRow = params[1];
            InputStream inputStream;
            String stockString = null;
            try {
                Uri builtUri = Uri.parse(baseUrl).buildUpon().appendPath(input + ".json").appendQueryParameter(ID_PARAM, APP_ID)
                        .appendQueryParameter(rows, numRow).appendQueryParameter(cols, numCol).build();
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
//                Log.v(LOG_TAG, stockString);
            } catch (IOException e) {
//                Log.e(LOG_TAG, "Bad site");
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
            if (Utility.getName() != null)
                mNameTView.setText(Utility.getName());
            if (Utility.getDate() != null)
                mDateTView.setText(Utility.getDate());
            if (Utility.getPrice() != null)
                mOutputTView.setText(Double.toString(Utility.getPrice()));
            if (Utility.getLineData() != null) {
                mChart.setData(Utility.getLineData());
                mChart.setDescription("");
                mChart.invalidate(); // refresh
            }
        }


    }


//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
