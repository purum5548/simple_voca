package com.danerdaner.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.danerdaner.Items.ListItem;
import com.danerdaner.Items.ScoreListItem;
import com.danerdaner.Items.categoryListItem;
import com.danerdaner.database.ScoreDatabase;
import com.danerdaner.database.VocaDatabase;
import com.danerdaner.database.categoryDatabase;
import com.danerdaner.simple_voca.R;
import com.danerdaner.simple_voca.VocaForegroundService;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoadingActivity extends AppCompatActivity {

    public static VocaDatabase vocaDatabase;
    public static categoryDatabase categoryDatabase;
    public static ScoreDatabase ScoreDatabase;

    public static ArrayList<ListItem> vocaList = new ArrayList<>();
    public static ArrayList<categoryListItem> categoryList = new ArrayList<>();
    public static ArrayList<ScoreListItem> categoryTestResultList = new ArrayList<>();
    public static ArrayList<ListItem> lockVocaList = new ArrayList<>();
    public static ArrayList<ListItem> vocaShuffleList = new ArrayList<>();

    public static String SELECTED_CATEGORY_NAME = "전체";
    public static String SELECTED_CATEGORY_SUBTITLE;

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1111;
    private static final int ACTION_MANAGE_READ_REQUEST_CODE = 2;

    public static SharedPreferences sharedPreferences;
    public static boolean WordShuffleCheck = false;


    @RequiresApi(api = Build.VERSION_CODES.O)
   // @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);

        WordShuffleCheck = false;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String lock_category_name = sharedPreferences.getString("category", "전체");

        vocaDatabase = new VocaDatabase(getApplicationContext(), "voca", null, 2);
        vocaDatabase.makeList(lockVocaList, lock_category_name);

        vocaDatabase.makeList(vocaShuffleList);
        Collections.shuffle(vocaShuffleList);
        vocaDatabase.makeList(vocaList);


        categoryDatabase = new categoryDatabase(getApplicationContext(), "category", null, 2);
        if(categoryDatabase.getSize() == 0){
            LoadingActivity.categoryDatabase.insert("전체", "모든 단어를 가지고 있는 단어장입니다.");
        }
        categoryDatabase.makeList(categoryList);
        SELECTED_CATEGORY_SUBTITLE = categoryDatabase.getCategorySubTitle(SELECTED_CATEGORY_NAME);

        ScoreDatabase = new ScoreDatabase(getApplicationContext(), "Score", null, 3);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();

        // 튜토리얼이 끝나면 권한을 획득하고, 획득하면 스플래쉬 아트 보여주고 앱 실행
        getPermission();
    }

    public void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우

            int permission_read = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            int permission_write = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if(permission_read == PackageManager.PERMISSION_DENIED ||
                    permission_write == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, ACTION_MANAGE_READ_REQUEST_CODE);
            }

            else {
                if (!Settings.canDrawOverlays(this)) {// 체크

                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                } else {
                    GoToNextActivity();
                }
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리
                finish();
            } else {
                GoToNextActivity();
            }
        }
    }

    private void GoToNextActivity(){
        new Handler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                // 서비스 인텐트 생성 후 서비스 실행. 이 때 오레오 이전 버전과 이후 버전에서 서비스를 시작하는 방식이 조금 다르다.
                if(sharedPreferences.getBoolean("service", false)){
                    Intent serviceIntent = new Intent(LoadingActivity.this, VocaForegroundService.class);
                    startForegroundService(serviceIntent);
                }

                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }


    @Override
    protected void onPause() {
        super.onPause();
        //finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

}
