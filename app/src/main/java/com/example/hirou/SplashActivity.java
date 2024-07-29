package com.example.hirou;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 액티비티가 화면에 표시되기 전에 상태 바를 숨깁니다.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.open); // 스플래시 화면으로 사용할 레이아웃 설정

        // 지연 후 메인 액티비티 시작
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity(); // 메인 액티비티 시작 메소드 호출
            }
        }, 3000); // 3초 동안 지연
    }

    // 메인 액티비티 시작 메소드
    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent); // 메인 액티비티 시작
        finish(); // 스플래시 액티비티를 종료하여 뒤로 가기 버튼을 눌렀을 때 다시 보이지 않게 함
    }
}