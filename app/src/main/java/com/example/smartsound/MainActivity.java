package com.example.smartsound;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{
    boolean flag =false;
    double dbCheck = 0;
    private long time= 0;
    TextView mStatusView;
    MediaRecorder mRecorder;
    Thread runner;
    NotificationManager manager;
    NotificationCompat.Builder builder;
    long now;
    Date currentTime;
    ArrayList<String> settingarray = new ArrayList();
    ArrayList<String> temparray = new ArrayList();

    private static String CHANNEL_ID = "channel1";
    private static String CHANEL_NAME = "Channel1";
    private static final String SETTINGS_PLAYER_JSON = "logsetting_item_json";
    private static final String SETTINGS_JSON = "setting_item_json";

    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;
    final Runnable updater = new Runnable() {

        public void run() {
            updateTv();
        }
    };
    final Handler mHandler = new Handler();
    Switch swdbcheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionCheck();

        mStatusView = (TextView) findViewById(R.id.mStatusView);
        Button SoundAdd = (Button) findViewById(R.id.btnsoundAdd);         //소리추가
        Button stt = (Button) findViewById(R.id.btngoogleStt);            //stt버튼
        Button vhouseholdbtn = (Button) findViewById(R.id.householdbtn); //가구 로그 버튼
        Button vMaker = (Button) findViewById(R.id.maker);
        Button vSetting = (Button) findViewById(R.id.setting);
        swdbcheck = findViewById(R.id.swdbcheck);
        settingarray = getStringArrayPref(getApplicationContext(), SETTINGS_JSON);



        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        };
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }

        SoundAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SoundAdd.class);
                startActivity(intent);

            }
        });

        stt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), GoogleStt.class);
                startActivity(intent);
            }
        });

        vhouseholdbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Loglist.class);
                startActivity(intent);
            }
        });

        vMaker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), maker.class);
                startActivity(intent);
            }
        });
        vSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), setting.class);
                startActivity(intent);
            }
        });
        swdbcheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    startRecorder();
                }else{
                    stopRecorder();
                    mStatusView.setText("0dB");
                }
            }
        });

    }

    public void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " + android.util.Log.getStackTraceString(ioe));

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
            try {
                mRecorder.start();
            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }

            //mEMA = 0.0;
        }

    }

    public void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void updateTv() {
        int dbstate;
        dbCheck = getAmplitudeEMA();
        settingarray = getStringArrayPref(getApplicationContext(), SETTINGS_JSON);
        double Decibelresult = soundDb(dbCheck);
        int Decibel = (int) Math.round(Decibelresult);

        if(swdbcheck.isChecked()){
            mStatusView.setText(Decibel+ " dB");
        }
        else
        {
            mStatusView.setText("0 dB");
        }
        try {
            if (settingarray.get(0) == null) {
                dbstate = 80;
            } else {
                dbstate = Integer.parseInt(settingarray.get(0));
            }
        }
        catch(Exception e){
            dbstate = 80;
        }
        if(Decibel > dbstate && !flag){
            flag = true;
            showNoti(+dbstate + " 데시벨 발생");
        }
        else if(Decibel <= dbstate){
            flag = false;
        }

    }

    public double soundDb(double ampl) {
        return 20 * Math.log10(ampl);      /////// *************** 계산식 수정
        //return 20 * Math.log10(getAmplitudeEMA() / ampl);
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude());
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }
    public void showNoti(String name){
        temparray = getStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON);
        builder = null;
        now = System.currentTimeMillis();
        currentTime = new Date(now);
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.getDefault()).format(currentTime);

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); //버전 오레오 이상일 경우
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel( new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );

            builder = new NotificationCompat.Builder(this,CHANNEL_ID); //하위 버전일 경우
        }else{
            builder = new NotificationCompat.Builder(this);
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("name",name);
        temparray.add(name + "," + date_text);
        setStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON, temparray);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        //알림창 제목
        builder.setContentTitle("알림");
        //알림창 메시지
        builder.setContentText(name);
        // 알림창 아이콘
        builder.setSmallIcon(R.drawable.ic_baseline);

        // 알림창 터치시 상단 알림상태창에서 알림이 자동으로 삭제되게 합니다.
        builder.setAutoCancel(true);
        //pendingIntent를 builder에 설정 해줍니다.
        // 알림창 터치시 인텐트가 전달할 수 있도록 해줍니다.
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        //알림창 실행
        manager.notify(1,notification); }

    public void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
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

    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<2000){
            finish();
        }
    }


}
