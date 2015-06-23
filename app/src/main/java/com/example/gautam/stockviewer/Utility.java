package com.example.gautam.stockviewer;

import android.util.Log;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Gautam on 6/23/2015.
 */
public class Utility {
    final static String LOG_TAG = "Utility";
    private static String name = null;
    private static String date = null;
    private static Double price = null;

    private static LineData lineData = null;

    public static void getTodaysPrice(String stockJsonStr) {
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

    public static void getDataRange(String stockJsonStr) {
        final String COL_NAMES = "column_names";
        final String DATA = "data";
        ArrayList<Entry> vals = new ArrayList<Entry>();
        try {
            JSONObject stockJson = new JSONObject(stockJsonStr);
            JSONArray names = stockJson.getJSONArray(COL_NAMES);
            JSONArray data = stockJson.getJSONArray(DATA);
            ArrayList<String> xVals = new ArrayList<String>();
            int len = data.length();
            for (int i = len - 1; i >= 0; i--) {
                JSONArray obj = data.getJSONArray(i);
                vals.add(new Entry((float) obj.getDouble(1), len - 1 - i));
//                    xVals.add(String.valueOf(len - i));
                xVals.add(obj.getString(0));
            }
            LineDataSet dataSet = new LineDataSet(vals, name);
            dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineData = new LineData(xVals, dataSet);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON Error!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getName() {
        return name;
    }

    public static String getDate() {
        return date;
    }

    public static Double getPrice() {
        return price;
    }

    public static LineData getLineData() {
        return lineData;
    }
}
