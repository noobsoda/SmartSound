package com.example.smartsound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SoundAdd extends AppCompatActivity implements MyListAdapter.ListChkboxClickListener {

    static final int REQ_ADD_CONTACT = 1;
    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";
    final ArrayList<list_item> items = new ArrayList<list_item>();
    ArrayList<String> temparray = new ArrayList();
    MyListAdapter adapter;
    long now;
    Date currentTime;

    @Override
    public void onListChkClick(int position) {
        //체크버튼이 체크되있는지 검사해서 값 변경하고 저장
        if (items.get(position).isChknum() == false) {
            items.set(position, new list_item(true, items.get(position).getUsetime(), items.get(position).getName(), items.get(position).getSize(), items.get(position).getWrite_date(), null));
        } else {
            items.set(position, new list_item(false, items.get(position).getUsetime(), items.get(position).getName(), items.get(position).getSize(), items.get(position).getWrite_date(), null));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_add);
        //빈 데이터 리스트 생성
        // ArrayAdapter 생성, 아이템 View를 선택가능하도록 만듦
        temparray = getStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON);
        adapter = new MyListAdapter(SoundAdd.this, items, this);
        //ListView 생성 및 adapter 지정
        final GridView gridView = (GridView) findViewById(R.id.listMenu);
        gridView.setAdapter(adapter);

        // 값 텍스트 불러오기
        for(int i = 0; i < temparray.size(); i++){
            String[] splited = temparray.get(i).split(",");
            try {
                items.add(new list_item(false, splited[0], splited[1], splited[2], splited[3], splited[4]));
            }
            catch(Exception e){
                items.add(new list_item(false, null, null, null, null, null));
            }
        }
        //그리드뷰를 클릭했을때 이벤트
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SoundAdd.this, sound_save.class);
                intent.putExtra("sound_name", items.get(position).getName());
                intent.putExtra("maxPoint", items.get(position).getUsetime());
                intent.putExtra("fileName", items.get(position).getFilename());
                intent.putExtra("position", position);

                startActivityForResult(intent, REQ_ADD_CONTACT);

            }
        });


        // add button에 대한 이벤트 처리.
        Button addButton = (Button) findViewById(R.id.btn_add);
        addButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                now = System.currentTimeMillis();
                currentTime = new Date(now);
                String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.getDefault()).format(currentTime);

                Intent intent = new Intent(SoundAdd.this, sound_save.class);
                intent.putExtra("position",items.size());
                startActivityForResult(intent, REQ_ADD_CONTACT);

            }
        });

        // delete button에 대한 이벤트 처리.
        Button deleteButton = (Button) findViewById(R.id.btn_sub);
        deleteButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count;
                count = items.size();
                for (int i = count - 1; i >= 0; i--) {
                    if (items.get(i).isChknum() == true) {
                        items.remove(i);
                        temparray.remove(i);
                        gridView.clearChoices();
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    //   액티비티 간 데이터 전달받는 메소드
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQ_ADD_CONTACT) {
            if (resultCode == RESULT_OK) {
                now = System.currentTimeMillis();
                currentTime = new Date(now);
                String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.getDefault()).format(currentTime);

                String soundname = intent.getStringExtra("editTextName");
                String maxPoint = intent.getStringExtra("textMaxPoint");
                String filename = intent.getStringExtra("fileName");
                String filesize = intent.getStringExtra("fileSize");
                int position = intent.getIntExtra("position", 0);
                if(position == items.size()){
                    items.add(new list_item(false, maxPoint, soundname, filesize, date_text, filename));
                    temparray.add(maxPoint + "," + soundname + "," + filesize + "," + date_text + "," + filename);
                }
                else {
                    items.set(position, new list_item(false, maxPoint, soundname, filesize, date_text, filename));
                    temparray.set(position, maxPoint + "," + soundname + "," + filesize + "," + date_text + "," + filename);
                }
                adapter.notifyDataSetChanged();

            }
        }
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

    // 앱이 멈췄을때 발생하는 이벤트
    @Override
    protected void onPause() {
        super.onPause();
        setStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON, temparray);
    }

}
