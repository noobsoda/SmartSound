package com.example.smartsound;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.tv.TvRecordingClient;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class sound_save extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    MediaRecorder recorder;
    String filename;

    MediaPlayer player;
    private static final int REC_STOP = 0;
    private static final int RECORDING = 1;
    private static final int PLAY_STOP = 0;
    private static final int PLAYING = 1;
    private static final int PLAY_PAUSE = 2;

    private int mRecState = REC_STOP;
    private int mPlayerState = PLAY_STOP;
    private SeekBar mRecProgressBar, mPlayProgressBar;
    private TextView mTvPlayMaxPoint;

    private int mCurRecTimeMs = 0;
    private int mCurProgressTimeDisplay = 0;



    Handler mProgressHandler = new Handler() {
        public void handleMessage(Message msg) {
            mCurRecTimeMs = mCurRecTimeMs + 100;
            mCurProgressTimeDisplay = mCurProgressTimeDisplay + 100;

            // 녹음시간이 음수이면 정지버튼을 눌러 정지시켰음을 의미하므로
            // SeekBar는 그대로 정지시키고 레코더를 정지시킨다.
            if (mCurRecTimeMs < 0) {
            }
            // 녹음시간이 아직 최대녹음제한시간보다 작으면 녹음중이라는 의미이므로
            // SeekBar의 위치를 옮겨주고 0.1초 후에 다시 체크하도록 한다.
            else if (mCurRecTimeMs < 60000) {
                mRecProgressBar.setProgress(mCurProgressTimeDisplay);
                mProgressHandler.sendEmptyMessageDelayed(0, 100);
            }
            // 녹음시간이 최대 녹음제한 시간보다 크면 녹음을 정지 시킨다.
            else {
                recordAudio();
            }
        }
    };

    // 재생시 SeekBar 처리
    Handler mProgressHandler2 = new Handler() {
        public void handleMessage(Message msg) {
            if (player == null)
                return;

            try {
                if (player.isPlaying()) {
                    mPlayProgressBar.setProgress(player.getCurrentPosition());
                    mProgressHandler2.sendEmptyMessageDelayed(0, 100);
                }
                else {
                    player.release();
                    player = null;

                    updateUI();
                }
            } catch (IllegalStateException e) {
            } catch (Exception e) {
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_save);
        permissionCheck();
        Intent intent = getIntent();

        //사운드이름 불러오는 코드
        EditText editTextName = (EditText)findViewById(R.id.txtsound_name);
        String soundname = intent.getStringExtra("sound_name");
        String maxpoint = intent.getStringExtra("maxPoint");
        filename = intent.getStringExtra("fileName");
        mRecProgressBar = (SeekBar) findViewById(R.id.recProgressBar);
        mPlayProgressBar = (SeekBar) findViewById(R.id.playProgressBar);
        mTvPlayMaxPoint = (TextView) findViewById(R.id.tvPlayMaxPoint);

        if(soundname != null)
            editTextName.setText(soundname);
        if(maxpoint != null)
        mTvPlayMaxPoint.setText(maxpoint);





        Log.d("MainActivity", "저장할 파일 명 : " + filename);

        findViewById(R.id.btnStartPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayerState == PLAY_STOP) {
                    mPlayerState = PLAYING;
                    initMediaPlayer();
                    playAudio();
                    updateUI();
                }
            }
        });

        findViewById(R.id.btnStopPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayerState == PLAYING) {
                    mPlayerState = PLAY_STOP;
                    stopAudio();
                    updateUI();
                }
            }
        });

        findViewById(R.id.btnStartRec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRecState == REC_STOP) {
                    SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    File file = new File(getApplicationContext().getFilesDir(), timeStampFormat.format(new Date()).toString() + "recorded.mp4");
                    filename = file.getAbsolutePath();
                    mRecState = RECORDING;
                    recordAudio();
                    updateUI();
                }
            }
        });

        findViewById(R.id.btnStopRec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRecState == RECORDING) {
                    mRecState = REC_STOP;
                    stopRecording();
                    closePlayer();
                    initMediaPlayer();
                    updateUI();

                }
            }
        });
        findViewById(R.id.btnsave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveReturn();
            }
        });

    }
    private void saveReturn(){
        Intent intent = getIntent();
        //알람 이름 리턴
        EditText editTextname = (EditText)findViewById(R.id.txtsound_name);
        if(!(editTextname.getText() == null)){
            intent.putExtra("editTextName", editTextname.getText().toString());
        }
        else{
            intent.putExtra("editTextName", " ");
        }
        //포지션 리턴
        intent.putExtra("position", intent.getIntExtra("position", 0));
        //녹음 시간 리턴
        intent.putExtra("textMaxPoint", mTvPlayMaxPoint.getText().toString());
        if(!(filename == null)){
            intent.putExtra("fileName", filename);
            intent.putExtra("fileSize", getFileSize(filename));
        }
        else{
            intent.putExtra("filename", "Empty");
            intent.putExtra("fileSize", 0);
        }
//        String strName = editTextname.getText().toString();
//        if(!strName.isEmpty() && strName.matches("^[0-9]8$")){
//            intent.putExtra("editTextname", strName);
//        }
//        else{
//            intent.putExtra("editTextname", 0);
//        }



        setResult(RESULT_OK, intent);
        finish();

    }
    private void recordAudio() {
        mCurRecTimeMs = 0;
        mCurProgressTimeDisplay = 0;

        mProgressHandler.sendEmptyMessageDelayed(0, 100);

        if(recorder == null){
            recorder = new MediaRecorder();
            recorder.reset();
        }
        else{
            recorder.reset();
        }

        /* 그대로 저장하면 용량이 크다.
         * 프레임 : 한 순간의 음성이 들어오면, 음성을 바이트 단위로 전부 저장하는 것
         * 초당 15프레임 이라면 보통 8K(8000바이트) 정도가 한순간에 저장됨
         * 따라서 용량이 크므로, 압축할 필요가 있음 */
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 어디에서 음성 데이터를 받을 것인지
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 압축 형식 설정
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        recorder.setOutputFile(filename);

        try {
            recorder.prepare();
            recorder.start();

            Toast.makeText(this, "녹음 시작됨.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try{
            recorder.stop();
        }
        catch(Exception e){

        }
        finally {
            recorder.release();
            recorder = null;
        }
        mCurRecTimeMs = -999;
        mProgressHandler.sendEmptyMessageDelayed(0,0);

        Toast.makeText(this, "녹음 중지됨.", Toast.LENGTH_SHORT).show();
    }

    private void initMediaPlayer(){
        if (player == null)
            player = new MediaPlayer();
        else
            player.reset();

        player.setOnCompletionListener(this);

        try {
            player.setDataSource(filename);
            player.prepare();
            int point = player.getDuration();
            mPlayProgressBar.setMax(point);

            int maxMinPoint = point / 1000 / 60;
            int maxSecPoint = (point / 1000) % 60;
            String maxMinPointStr = "";
            String maxSecPointStr = "";

            if (maxMinPoint < 10)
                maxMinPointStr = "0" + maxMinPoint + ":";
            else
                maxMinPointStr = maxMinPoint + ":";

            if (maxSecPoint < 10)
                maxSecPointStr = "0" + maxSecPoint;
            else
                maxSecPointStr = String.valueOf(maxSecPoint);

            mTvPlayMaxPoint.setText(maxMinPointStr + maxSecPointStr);
        } catch (Exception e) {
            Log.v("ProgressRecorder", "미디어 플레이어 Prepare Error ==========> " + e);
        }
    }

    private void playAudio() {
        try {

            // SeekBar의 상태를 0.1초마다 체크
            mProgressHandler2.sendEmptyMessageDelayed(0, 100);
            player.start();
            Toast.makeText(this, "플레이 시작됨.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void stopAudio() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
            player = null;
            mPlayProgressBar.setProgress(0);

            // 즉시 SeekBar 메세지 핸들러를 호출한다.
            mProgressHandler2.sendEmptyMessageDelayed(0, 0);
            Toast.makeText(this, "중지됨.", Toast.LENGTH_SHORT).show();
        }
    }

    public void closePlayer() {
        if (player != null) {
            player.release();
            player = null;
            mPlayProgressBar.setProgress(0);
        }
    }
    private void updateUI() {
        if (mRecState == REC_STOP) {
            mRecProgressBar.setProgress(0);
        }

        if (mPlayerState == PLAY_STOP) {
            mPlayProgressBar.setProgress(0);
        }
    }
    public String getFileSize(String filename){
        String size = "";
        File mFile = new File(filename);
        if(mFile.exists()) {
            long iFileSize = mFile.length()/1024;

            size = Long.toString(iFileSize) + "kb";

        }
        else{
            size = "0kb";
        }
        return size;
    }

    // 권한 체크
    public void permissionCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayerState = PLAY_STOP; // 재생이 종료됨

        // 재생이 종료되면 즉시 SeekBar 메세지 핸들러를 호출한다.
        mProgressHandler2.sendEmptyMessageDelayed(0, 0);
        updateUI();
    }
}

