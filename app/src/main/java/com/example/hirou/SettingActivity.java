package com.example.hirou;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SettingActivity extends AppCompatActivity {
    int saveGoal; // 목표치 설정 스피너로 사용자에게 받은 값을 sharedPreferences에 저장할 때 쓰는 변수
    Spinner spinnerGoal, spinnerTumblerWeight; // 목표치 설정하는 Spinner
    boolean mlnitSpinner; // Spinner 첫 선택 방지 변수
    private TimePicker timePicker; // 타임피커 위젯 변수
    private TextView showSelectedtime; // TimePicker로 선택된 시간을 표시하는 TextView 변수
    Button buttonSetTime;  // 알림 시간 설정 버튼
    private TextView notificationSettingsText; // "알림 설정" TextView에 onClick() 구현하기 위한 TextView 변수
    private boolean isTextVisible = false; // visible, invisible 상태를 변환을 위한 변수
    Calendar calendar; // 알림 발생, 토스트 메시지 출력할 때 쓰는 날짜, 시간 관련 클래스
    int h, m; // 선택된 알림 시간 표시하는 토스트 메시지 출력에 사용
    ImageButton informationButton; // 팝업으로 정보창 띄우기 위한 ImageButton 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        MyApp myApp = (MyApp) getApplication();


        setupSpinner(); // Spinner xml 연결, Spinner item 생성, Spinner 스타일 정의
        spinnerGoal.setSelection(myApp.getGoalSpinnerItem());
        initializeSpinnerSelection(); // Spinner item 선택되면 이벤트 발생

        // xml 연결
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        showSelectedtime = (TextView) findViewById(R.id.showSelectedTime);
        showSelectedtime.setText(myApp.getSelectedTime());
        buttonSetTime = findViewById(R.id.buttonSetTime);

        setupTimePicker(); // TimePicker를 24시간제로 설정f
        initializeAndDisplayNotiTime(); // 이전 설정 시간 표시, Calendar 초기 설정, 초기 토스트 메시지 출력
        changedTimePicker(); // TimePicker 변화에 따른 설정 메소드
        setupNotiTimeButton(); // 시간 확정, 알림 설정 버튼
        showAndHideTimePicker(); // 알림 설정 TextView를 클릭하면 TimePicker, TextView, Button 보이거나 안 보이게 하는 메소드
        showInformation(); // 하이루 정보 팝업창

        // 버튼 텍스트 크기 설정
        buttonSetTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        showSelectedtime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        // 뒤로 가기 버튼 설정
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setupSpinner() { // 스피너 설정
        spinnerGoal = findViewById(R.id.spinnerGoal);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.water_goals, android.R.layout.simple_spinner_item); // 스피너 item은 res.values.arrays.xml에 있음
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);

    }

    /*private void setupTumblerWeight() { // 텀블러 무게 설정 -> 초기화 버튼의 추가로 텀블러의 무게를 미리 설정해 놓을 필요가 없어짐
        spinnerTumblerWeight = findViewById(R.id.spinnerTumblerWeight);
        ArrayAdapter<CharSequence> tumblerWeightAdapter = ArrayAdapter.createFromResource(this,
                R.array.tumbler_weight_options, android.R.layout.simple_spinner_item);
        tumblerWeightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTumblerWeight.setAdapter(tumblerWeightAdapter);
    }*/

    private void initializeSpinnerSelection() {
        // 스피너 아이템 선택 이벤트 처리
        spinnerGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handleSpinnerSelection(parent, position); // 스피너 값 선택되면 sharedPreferences에 데이터 저장, 토스트 메시지 출력하는 메소드

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { // 이건 보류... 아직 구현 X
                // 아무것도 선택되지 않았을 때의 처리 (선택적)
            }
        });
    }

    private void handleSpinnerSelection(AdapterView<?> parent, int position) {
        MyApp myApp = (MyApp) getApplication(); // MyApp.java의 전역 메소드 사용을 위해 필요


        // 선택된 아이템의 값을 saveGoal 변수에 저장 후 setGoalAmount() 메소드 이용 (sharedPreferences로 연결)
        saveGoal = Integer.parseInt(parent.getItemAtPosition(position).toString()); // saveGoal 변수에 spinner로 선택된 값 저장
        myApp.setGoalAmount(saveGoal); // 목표치 값 sharedPreferences에 저장
        myApp.getTodayAmount(); // 마신량 최신화        ***(여기서 최신화하는 것이 맞는지 블루투스 구현 시 다시 생각해봐야 함.)***

        myApp.setGoalSpinnerItem(position);

        if (myApp.getTodayAmount() < myApp.getGoalAmount()) { // 마신량이랑 목표치 비교해서
            checkAndNotifyForCurrentAmount(); // 알림 발생      ***앱 완성하면 필요 X, 지금은 목표치 변화량에 따른 알림 발생 보기 위해 넣어놓은 것.
        }
        if (mlnitSpinner == false) { // 처음 선택되었을 때 알림 메시지 안 뜨도록
            mlnitSpinner = true;
            return;
        }
        // 선택된 item(목표치)을 나타내는 toast 메시지
        Toast.makeText(parent.getContext(), "목표량 설정 : " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
    }

    private void setupTimePicker() {
        timePicker.setIs24HourView(true); // 24시간제
    }

    private void initializeAndDisplayNotiTime() {
        // 이전 알림 설정한 값을 기억해서 보여주고, 없을시 현재시간이 보여짐
        SharedPreferences savedTime = getSharedPreferences("DailyAlarm", MODE_PRIVATE);
        long millis = savedTime.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());

        // 그래고리안 달력 사용 (윤년 반영) -> 정확한 시간을 반영할 수 있다.
        Calendar nextNotifyTime = new GregorianCalendar();
        nextNotifyTime.setTimeInMillis(millis);


        setCurrentTimeOnPicker(nextNotifyTime); // 초기 타임피커에 현재 시간 반영
    }

    private void setCurrentTimeOnPicker(Calendar nextNotifyTime) {
        // 안드로이드 버전에 따라 다르게 적용해주기 위한 작업
        Date currentTime = nextNotifyTime.getTime(); // 현재 설정된 시간 받기
        SimpleDateFormat HourFormat = new SimpleDateFormat("HH", Locale.getDefault());
        SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

        int preHour = Integer.parseInt(HourFormat.format(currentTime));
        int preMinute = Integer.parseInt(MinuteFormat.format(currentTime));

        //sdk 버전에 다라 다르게 타임 피커에 시간 적용 시켜주기
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(preHour);
            timePicker.setMinute(preMinute);
        } else {
            timePicker.setCurrentHour(preHour);
            timePicker.setCurrentMinute(preMinute);
        }
    }

    private void changedTimePicker() { // TimePicker 시간 변화 이벤트 설정 메소드
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                // 오전, 오후 를 확인하기 위한 if 문

                if (hour > 12) {
                    h = hour;
                    hour -= 12;
                    showSelectedtime.setText("오후 " + hour + "시 " + minute + "분으로 변경"); // 타임피커에 선택된 시간 표시
                } else {
                    h = hour;
                    showSelectedtime.setText("오전 " + hour + "시 " + minute + "분으로 변경");
                }
                m = minute;
            }
        });
    }

    private void setupNotiTimeButton() {

        buttonSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApp myApp = (MyApp) getApplication();

                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, h);
                calendar.set(Calendar.MINUTE, m);
                calendar.set(Calendar.SECOND, 0);

                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }

                Date currentDateTime = calendar.getTime();
                String dateText = new SimpleDateFormat("d일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                Toast.makeText(getApplicationContext(), dateText + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                String savedTime = new SimpleDateFormat("설정된 알람 : a h시 m분 ", Locale.getDefault()).format(currentDateTime);
                myApp.setSelectedTime(savedTime);
                showSelectedtime.setText(savedTime);

                // Preference에 설정한 값 저장
                SharedPreferences.Editor editor = getSharedPreferences("DailyAlarm", MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime", (long) calendar.getTimeInMillis());
                editor.apply();

                diaryNotification(calendar); // 매일 설정한 시간에 알림 울리기 위한 메소드

            }
        });
    }

    // "알림 설정" TextView를 클릭하면 아래에 TimePicker, TextView, Button이 나타나면서 나머지 위젯은 아래로 내려가는 알고리즘
    private void showAndHideTimePicker() {
        ConstraintLayout layout = findViewById(R.id.activitySetting); // 설정창에서 이 효과를 주는 것.
        notificationSettingsText = findViewById(R.id.notificationSettingsText); // TextViewm에 OnClick() 쓸 예정.

        notificationSettingsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(layout); // 효과가 필요한(설정창)에 애니메이션을 줄 수 있는 메소드 이용(?)
                timePicker.setVisibility(isTextVisible ? View.GONE : View.VISIBLE); // 클릭되면 나타남.
                showSelectedtime.setVisibility(isTextVisible ? View.GONE : View.VISIBLE);
                buttonSetTime.setVisibility(isTextVisible ? View.GONE : View.VISIBLE);
                isTextVisible = !isTextVisible; // visible, invisible을 통제하는 변수 값 조정
            }
        });
    }


    private void showInformation() { // 하이루란? 버튼 설정
        informationButton = (ImageButton) findViewById(R.id.informationButton);

        informationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝업창을 띄우는 코드
                Dialog dialog = new Dialog(SettingActivity.this, R.style.RoundedDialog);
                dialog.setContentView(R.layout.popup_activity);
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
    }

    void diaryNotification(Calendar calendar) { // 매일 설정한 시간에 알림 울리기 위한 메소드

        MyApp myApp = (MyApp) getApplication();
        Boolean dailyNotify = true; // 매일 반복되는 알람의 조건 변수(항상울림)

        PackageManager pm = this.getPackageManager();
        //ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 매일 울리기 위한 부분
        if (dailyNotify) {
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
            // 부팅 후 실행되는 리시버 사용가능하게 설정
            //pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }

    // 목표치 변경에 따른 알림 보기 위한 메소드. 앱 정식 출시에는 필요 X
    private void checkAndNotifyForCurrentAmount() {
        // 알림 생성 및 발송 코드
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = new Notification.Builder(this, "GOALATTAINMENT_CHANNEL_ID") // 채널 ID 필요 (안드로이드 O 이상)
                .setContentTitle("스피너 item 선택으로 인한 미달성 알림")
                .setContentText("알람 확인용. 정식 출시할 때는 지울 것.")
                .setSmallIcon(R.drawable.hirue)
                .setAutoCancel(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // 안드로이드 O 이상에서는 알림 채널 설정 필요
            NotificationChannel channel = new NotificationChannel("GOALATTAINMENT_CHANNEL_ID", "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, builder.build());
    }
}