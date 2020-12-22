package com.example.smartsound;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class setting extends AppCompatActivity {
    private static final String SETTINGS_PLAYER_JSON = "setting_item_json";
    ArrayList<String> settingarray = new ArrayList();
    TextView dbtext;
    SeekBar sbdbstate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sbdbstate = findViewById(R.id.seekbar_dbsetting);
        dbtext = findViewById(R.id.txt_dbstate);
        settingarray = getStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON);

        if(settingarray.size() == 0)
            settingarray.add("80");

        sbdbstate.setProgress(Integer.parseInt(settingarray.get(0)));
        dbtext.setText(String.valueOf(sbdbstate.getProgress()));
        // OnSeekBarChange 리스너 - Seekbar 값 변경시 이벤트처리 Listener
        sbdbstate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // onProgressChange - Seekbar 값 변경될때마다 호출
                dbtext.setText(String.valueOf(seekBar.getProgress()));
                settingarray.set(0, String.valueOf(seekBar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // onStartTeackingTouch - SeekBar 값 변경위해 첫 눌림에 호출
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // onStopTrackingTouch - SeekBar 값 변경 끝나고 드래그 떼면 호출
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

        setStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON, settingarray);
    }


}
