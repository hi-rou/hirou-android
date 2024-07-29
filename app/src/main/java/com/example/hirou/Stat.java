package com.example.hirou;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class Stat extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp myApp = (MyApp) getApplication();

        // 액티비티가 화면에 표시되기 전에 상태 표시줄을 숨깁니다.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // stat.xml 레이아웃 로드
        setContentView(R.layout.stat);


        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.MONDAY) {
            TextView textViewWeeklyStats = findViewById(R.id.textViewWeeklyStats);
            String lastWeek = "지난 주 통계";
            textViewWeeklyStats.setText(lastWeek);
        }


        // 뒤로 가기 버튼을 MainActivity로 돌아가도록 설정합니다.
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Stat.this, MainActivity.class);
                startActivity(intent);
                finish(); // 현재 액티비티를 종료하여 백 스택에서 제거합니다.
            }
        });

        // 통계 버튼 설정
        ImageButton statButton = findViewById(R.id.statButton);
        statButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝업창을 띄우는 코드
                Dialog dialog = new Dialog(Stat.this, R.style.RoundedDialog);
                dialog.setContentView(R.layout.popup_info);
                Window window = dialog.getWindow();
                WindowManager.LayoutParams params = window.getAttributes();

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                int height = displayMetrics.heightPixels;

                params.width = (int) (width * 0.85);
                params.height = (int) (height * 0.7);
                params.gravity = Gravity.CENTER;

                window.setAttributes(params);
                dialog.show();
            }
        });

        // 인텐트로부터 물 섭취 데이터를 받아옵니다.
        // 실제 데이터 대신 모의 데이터를 사용합니다.
        int[] waterIntakeData = {myApp.getMon(), myApp.getTue(), myApp.getWed(), myApp.getThu(), myApp.getFri(), myApp.getSat(), myApp.getSun()};

        // 요일 이름 배열
        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        // ListView에 표시할 데이터를 준비합니다.
        ArrayList<String> displayList = new ArrayList<>();
        for(int i = 0; i < waterIntakeData.length; i++) {
            String displayText = daysOfWeek[i] + ": " + waterIntakeData[i] + "ml";
            displayList.add(displayText);
        }

        int totalIntake = 0;
        for (int dailyIntake : waterIntakeData) {
            totalIntake += dailyIntake;
        }

        // ProgressBar 설정
        ProgressBar progressBar = findViewById(R.id.progressBarWaterIntake);
        int weeklyGoal = myApp.getGoalAmount() * 7; // 일주일 목표량 (2000ml x 7일)
        progressBar.setMax(weeklyGoal); // ProgressBar의 최대치를 일주일 목표량으로 설정
        progressBar.setProgress(totalIntake); // 현재 진행 사항을 계산된 총 섭취량으로 설정


        // ArrayAdapter를 사용하여 데이터를 ListView에 바인딩합니다.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.list_item_water_intake, displayList);

        ListView listView = findViewById(R.id.listViewWaterIntake);
        listView.setAdapter(adapter);

        TextView tvTotalIntake = findViewById(R.id.tvTotalIntake);
        tvTotalIntake.setText(" Total weekly intake : " + totalIntake + "ml / " + weeklyGoal + "ml"); // 실시간으로 변하는 총 섭취량 표시
    }
}//그레고리안의 사용으로 세계 각지의 어느 나라든 정확한 날짜에 값을 입력할 수 있다.
