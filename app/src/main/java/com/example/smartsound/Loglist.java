package com.example.smartsound;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Loglist extends AppCompatActivity {
    private GridView gridview;
    private static final String SETTINGS_PLAYER_JSON = "logsetting_item_json";
    final ArrayList<list_item> items = new ArrayList<list_item>();
    ArrayList<String> temparray = new ArrayList();
    LogListAdapter adapter;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loglist);
        gridview = (GridView)findViewById(R.id.loggridview);

        temparray = getStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON);
        adapter = new LogListAdapter(Loglist.this, items);
        gridview.setAdapter(adapter);

        // 값 텍스트 불러오기
        for(int i = 0; i < temparray.size(); i++){
            String[] splited = temparray.get(i).split(",");
            try {
                items.add(new list_item(splited[0], splited[1]));
            }
            catch(Exception e){
                items.add(new list_item(null, null));
            }
        }

        ImageButton deleteButton = (ImageButton) findViewById(R.id.imgbtn_delete);
        deleteButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count;
                count = items.size();
                for (int i = count - 1; i >= 0; i--) {
                    items.remove(i);
                    temparray.remove(i);
                    gridview.clearChoices();
                    adapter.notifyDataSetChanged();
                }
                setStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON, temparray);
            }
        });
    }
    // 공유메모리 사용해서 텍스트 저장하는 메소드
    private void setStringArrayPref(Context context, String key, ArrayList<String> values) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();

        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }

        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }

        editor.apply();
    }

    // 공유메모리 사용해서 텍스트 불러오는 메소드
    private ArrayList getStringArrayPref(Context context, String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList urls = new ArrayList();

        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);

                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }


    @Override
    protected void onPause() {
        super.onPause();


    }

}
