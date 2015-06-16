package com.example.gautam.stockviewer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = this.getClass().getSimpleName();

    private Button mBtn;
    private EditText mInputEText;
    private EditText mNumberEText;
    private TextView mOutputTView;
    private TextView mDateTView;
    private TextView mNameTView;
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = (Button) findViewById(R.id.search_btn);
        mInputEText = (EditText) findViewById(R.id.input);
        mNameTView = (TextView) findViewById(R.id.name);
        mOutputTView = (TextView) findViewById(R.id.price_today);
        mDateTView = (TextView) findViewById(R.id.date_today);
        mChart = (LineChart) findViewById(R.id.chart);
        mNumberEText = (EditText) findViewById(R.id.number);
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = mInputEText.getText().toString();
                String number = mNumberEText.getText().toString();
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (input.length() == 0) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please input a stock first", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (networkInfo != null && networkInfo.isConnected()) {
                    new getStockInfo().execute(input, number);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "You are not connected to the Internet", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class getStockInfo extends AsyncTask<String, Void, String> {
        final String baseUrl = "https://www.quandl.com/api/v1/datasets/WIKI/";
        final String APP_ID = "rNviB11yaDTGF8ycmsfY";
        final String ID_PARAM = "auth_token";
        final String rows = "rows";
        final String cols = "column";
        String numRow = "1";
        final String numCol = "4";

        final String LOG_TAG = this.getClass().getSimpleName();

        String name = null;
        String date = null;
        Double price = null;

        LineData lineData = null;

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
                getTodaysPrice(stockString);
                getDataRange(stockString);
                Log.v(LOG_TAG, stockString);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error no internet?");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return stockString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (name != null)
                mNameTView.setText(name);
            if (date != null)
                mDateTView.setText(date);
            if (price != null)
                mOutputTView.setText(Double.toString(price));
            if (lineData != null) {
                mChart.setData(lineData);
                mChart.invalidate(); // refresh
            }
        }

        private void getTodaysPrice(String stockJsonStr) {
            final String COL_NAMES = "column_names";
            final String DATA = "data";
            final String NAME = "name";
            try {
                JSONObject stockJson = new JSONObject(stockJsonStr);
                JSONArray names = stockJson.getJSONArray(COL_NAMES);
                JSONArray data = stockJson.getJSONArray(DATA).getJSONArray(0);
                name = stockJson.getString(NAME);
                name = name.substring(0, name.indexOf(")") + 1);
                date = data.getString(0);
                price = data.getDouble(1);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Error!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void getDataRange(String stockJsonStr) {
            final String COL_NAMES = "column_names";
            final String DATA = "data";
            ArrayList<Entry> vals = new ArrayList<Entry>();
            try {
                JSONObject stockJson = new JSONObject(stockJsonStr);
                JSONArray names = stockJson.getJSONArray(COL_NAMES);
                JSONArray data = stockJson.getJSONArray(DATA);
                ArrayList<String> xVals = new ArrayList<String>();
                int len = data.length();
                for (int i = data.length() - 1; i >= 0; i--) {
                    JSONArray obj = data.getJSONArray(i);
                    vals.add(new Entry((float) obj.getDouble(1), len - i));
                    xVals.add(String.valueOf(len - i));
                }
                LineDataSet dataSet = new LineDataSet(vals, name);
                dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                lineData = new LineData(xVals, dataSet);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Error!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
