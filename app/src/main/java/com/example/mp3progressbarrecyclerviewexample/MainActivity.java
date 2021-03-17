package com.example.mp3progressbarrecyclerviewexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Button btnPlay, btnStop, btnPause;
    private TextView tvMP3;
    private ProgressBar pbMP3;

    private ArrayList<MusicData> sdCardList = new ArrayList<MusicData>();

    private MediaPlayer mPlayer;

    private  int selectPosition;

    private MusicAdapter musicAdapter;

    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);

        findViewByIdFunc();
        findContentProviderMP3ToArrayList();

        musicAdapter=new MusicAdapter(getApplicationContext(), sdCardList);
        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(musicAdapter);

        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(btnPlay.isEnabled() == true) {
                    selectPosition = position;
                    btnPlay.setEnabled(true);
                    btnStop.setEnabled(false);
                    btnPause.setEnabled(false);

                    tvMP3.setText("노래준비할 음악" + sdCardList.get(position).getTitle());
                }else{
                    Toast.makeText(MainActivity.this, "노래중지하고 선택요망", Toast.LENGTH_SHORT).show();
                }
            }
        });

        selectPosition = 0;
        btnPlay.setEnabled(true);
        btnStop.setEnabled(false);
        btnPause.setEnabled(false);

        btnPlay.setOnClickListener(v ->{
            mPlayer=new MediaPlayer();
            MusicData musicData = sdCardList.get(selectPosition);
            Uri musicUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicData.getId());
            try {
                mPlayer.setDataSource(MainActivity.this, musicUri);
                mPlayer.prepare();
                mPlayer.start();

                btnPlay.setEnabled(false);
                btnStop.setEnabled(true);
                btnPause.setEnabled(true);
                tvMP3.setText("실행중인음악"+musicData.getTitle());
                pbMP3.setVisibility(View.VISIBLE);
            } catch (IOException e) {
               Log.d("MainActivity", "음악파일로드실패");
            }

        });

        btnStop.setOnClickListener(v ->{
            mPlayer.stop();
            mPlayer.reset();

            btnPlay.setEnabled(true);
            btnStop.setEnabled(false);
            btnPause.setEnabled(false);
            tvMP3.setText("음악선택 기다림");
            pbMP3.setVisibility(View.INVISIBLE);
        });

        btnPause.setOnClickListener(v -> {
            if(flag == false){
                mPlayer.pause();
                btnPause.setText("이어듣기");
                pbMP3.setVisibility(View.INVISIBLE);
                flag = true;
            }else{
                mPlayer.start();
                btnPause.setText("일시정지");
                pbMP3.setVisibility(View.VISIBLE);
                flag = false;
            }
        });


    }

    private void findContentProviderMP3ToArrayList() {
        // 컨텐트 프로바이더에서는 핸드폰에서 다운로드했던 음악파일은 모두 관리되고 있다.
        String[] data = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};

        // 전체 영역에서 음악파일 가져온다.
        Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                data,null,null,data[2] + " ASC");    // Cursor타입이 반환됨, 원하는 정보를 찾아줌
        // data가 내가 보고 싶은 항목, 그리고 TITLE 항목으로 오름차순으로 가져와라
        if(cursor != null){
            while(cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(data[0]));
                String artist = cursor.getString(cursor.getColumnIndex(data[1]));
                String title = cursor.getString(cursor.getColumnIndex(data[2]));
                String albumArt = cursor.getString(cursor.getColumnIndex(data[3]));
                String duration = cursor.getString(cursor.getColumnIndex(data[4]));

                MusicData musicData = new MusicData(id,artist,title,albumArt,duration);
                sdCardList.add(musicData);
            }   // end of while
        }
    }   // end of findContentProviderMP3ToArrayList


    private void findViewByIdFunc() {
        recyclerView = findViewById(R.id.recyclerView);
        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        btnPause = findViewById(R.id.btnPause);
        pbMP3 = findViewById(R.id.pbMP3);
        tvMP3 = findViewById(R.id.tvMP3);
    }
}