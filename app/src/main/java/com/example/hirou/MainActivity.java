package com.example.hirou;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ImageButton informationButton;                                                          //findViewById 사용 전 private 로 미리 선언해주어야한다.
    private Switch switch1, switch2;
    private FrameLayout lightThemeLayout, darkThemeLayout;
    private ImageView buttonSetting, statisticsIcon, bluetoothIcon, reloadIcon;
    private TextView nowAmount, waterAmountText, soFarText, farText, settingTextView, bluetoothTextView, statsTextView, refreshTextView;
    private MyApp myApp;
    private int currentAmount = 0;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {                                           //nonnull > 이 값은 널값을 허용하지 않는다.
            if (BluetoothService.readValue != null) {
                double readValue = BluetoothService.readValue;                                      //아두이노로 부터 값을 읽어 온다.
                int converted = (int) (readValue * 1000);                                           //아두이노로 부터 값을 kg 단위로 읽어 온다.
                myApp.reloadAmount(converted);                                                      //물의 양 새로 고침
                waterAmountText.setText(myApp.getTodayAmount() + "mL");                             //오늘 마신 물 총 량
                nowAmount.setText(converted + "mL");                                                //현재 텀블러에 담긴 물의 양
            }
            setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());
        }
    };

    private final Handler handlerdrain = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (BluetoothService.readValue != null) {
                double readValue = BluetoothService.readValue;
                int converted = (int) (readValue * 1000);
                myApp.drainAmount(converted);
                waterAmountText.setText(myApp.getTodayAmount() + "mL");
                nowAmount.setText(myApp.getBeforeAmount() + "mL");
            }
            setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());
        }
    };

    private final Handler handlerdrink = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (BluetoothService.readValue != null) {
                double readValue = BluetoothService.readValue;
                int converted = (int) (readValue * 1000);
                myApp.drinkAmount(converted);
                myApp.setTodayAmount(converted);
                waterAmountText.setText(myApp.getTodayAmount() + "mL");
                nowAmount.setText(converted + "mL");
            }
            setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());
        }
    };

    private BluetoothService bluetoothService;                                                      //블루투스 서비스에 관하여 (블루투스 연결 시에 다른 페이지로 넘어가면 블루투스가 끊어지는 상황이 발생)
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
            isBound = true;
            Log.d("BluetoothService", "ServiceConnection-onServiceConnected");             //서비스가 연결됐을 시 모든 페이지에 서비스를 연결할 수 있도록 허용
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;                                                                        //서비스가 연결되지 않을 시 false
        }
    };

    private static final int REQUEST_PERMISSIONS = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myApp = (MyApp) getApplication();

        // Request necessary permissions
        requestPermissions();

        createNotificationChannel();

        // Initialize views
        switch1 = findViewById(R.id.switch1);                                                       //findViewById는 xml에서 id를 가져와 활용하겠다는 의미
        switch2 = findViewById(R.id.switch2);
        lightThemeLayout = findViewById(R.id.lightThemeLayout);
        darkThemeLayout = findViewById(R.id.darkThemeLayout);
        nowAmount = findViewById(R.id.nowAmount);
        waterAmountText = findViewById(R.id.waterAmountText);
        soFarText = findViewById(R.id.soFarText);
        farText = findViewById(R.id.farText);
        settingTextView = findViewById(R.id.settingTextView);
        bluetoothTextView = findViewById(R.id.bluetoothTextView);
        statsTextView = findViewById(R.id.statsTextView);
        refreshTextView = findViewById(R.id.refreshTextView);
        ImageView button = findViewById(R.id.button);
        ImageView button2 = findViewById(R.id.button2);
        buttonSetting = findViewById(R.id.buttonSetting);
        statisticsIcon = findViewById(R.id.statisticsIcon);
        bluetoothIcon = findViewById(R.id.bluetoothIcon);
        reloadIcon = findViewById(R.id.reloadIcon);
        informationButton = findViewById(R.id.informationButton);
        Button format = findViewById(R.id.format);

        // activity_main.xml의 TextView에 목표량 연결
        TextView purposewaterAmountText = findViewById(R.id.PurposewaterAmountText);
        String goalAmountText = "GOAL " + String.valueOf((float) myApp.getGoalAmount() / 1000 + "L");
        purposewaterAmountText.setText(goalAmountText);

        waterAmountText.setText(myApp.getTodayAmount() + "mL");
        nowAmount.setText(myApp.getBeforeAmount() + "mL");

        setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());

        createNotificationChannel();
        resetAlarm(MainActivity.this);

        // Bind to BluetoothService
        Intent serviceIntent = new Intent(this, BluetoothService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Set the initial theme to light mode
        setInitialTheme();

        format.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (myApp.getDayValueforMain()) {
                    case 1:
                        myApp.setMon(myApp.getTodayAmount());                                       // 첫 시작은 월요일 나머지 값은 무조건 0
                        myApp.setTue(0);
                        myApp.setWed(0);
                        myApp.setThu(0);
                        myApp.setFri(0);
                        myApp.setSat(0);
                        myApp.setSun(0);
                        break;
                    case 2:
                        myApp.setTue(myApp.getTodayAmount());
                        break;
                    case 3:
                        myApp.setWed(myApp.getTodayAmount());
                        break;
                    case 4:
                        myApp.setThu(myApp.getTodayAmount());
                        break;
                    case 5:
                        myApp.setFri(myApp.getTodayAmount());
                        break;
                    case 6:
                        myApp.setSat(myApp.getTodayAmount());
                        break;
                    case 7:
                        myApp.setSun(myApp.getTodayAmount());
                        break;
                    default:
                        break;
                }
                myApp.setTodayAmount(0);                                                            // TodayAmount 로 부터 값을 가져온다.
                myApp.addDayValueForMain();

                TextView waterAmountText = findViewById(R.id.waterAmountText);
                String todayAmountText = String.valueOf(myApp.getTodayAmount()) + "mL";
                waterAmountText.setText(todayAmountText);

                int percentage = (int) ((float) myApp.getTodayAmount() / myApp.getGoalAmount() * 100);
                TextView amountPercent = findViewById(R.id.amountPercent);
                String percent = String.valueOf(percentage) + "%";
                amountPercent.setText(percent);

                //영점 조절 -> 일종의 텀블러 값을 초기화해주는 기능인데, 우리는 이 기능 말고 초기화 버튼을 추가했음
                if (isBound) {
                    bluetoothService.sendData("A");
                }
            }
        });

        // Set click listeners
        button.setOnClickListener(v -> {
            // Check if the service is bound
            if (!isBound) {
                Toast.makeText(getApplicationContext(), "Please connect to Bluetooth first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the BluetoothService is available
            if (bluetoothService == null) {
                Toast.makeText(getApplicationContext(), "Bluetooth service not available.", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Bluetooth service is null");
                return;
            }

            // Check if the characteristic is initialized properly
            if (bluetoothService.isCharacteristicInitialized()) {  // Add a method in BluetoothService to check this
                bluetoothService.sendData("B");
            } else {
                Toast.makeText(getApplicationContext(), "Please connect to Bluetooth first.", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Characteristic is not initialized in BluetoothService");
                return;
            }//Characteristic not initialized.

            myApp.getTodayAmount();
            if (bluetoothService != null) {
                bluetoothService.readData();
                Message message = handlerdrain.obtainMessage();
                handlerdrain.sendMessage(message);
            }

            TextView waterAmountText = findViewById(R.id.waterAmountText);
            String todayAmountText = String.valueOf(myApp.getTodayAmount()) + "mL";
            waterAmountText.setText(todayAmountText);

            setMainTextVeiw(myApp.getTodayAmount(), myApp.getGoalAmount(), myApp.getBeforeAmount());

            Toast.makeText(getApplicationContext(), "water away!", Toast.LENGTH_SHORT).show();

            // Send data 'A' to Arduino (if needed in the future)
        });

        button2.setOnClickListener(view -> { // 물 채움
            // Check if the service is bound
            if (!isBound) {
                Toast.makeText(getApplicationContext(), "Please connect to Bluetooth first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the BluetoothService is available
            if (bluetoothService == null) {
                Toast.makeText(getApplicationContext(), "Bluetooth service not available.", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Bluetooth service is null");
                return;
            }

            // Check if the characteristic is initialized properly
            if (bluetoothService.isCharacteristicInitialized()) {  // Add a method in BluetoothService to check this
                bluetoothService.sendData("B");
            } else {
                Toast.makeText(getApplicationContext(), "Please connect to Bluetooth first.", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Characteristic is not initialized in BluetoothService");
                return;
            }//Characteristic not initialized.

            myApp.getTodayAmount();
            if (bluetoothService != null) {
                bluetoothService.readData();
                Message message = handlerdrink.obtainMessage();
                handlerdrink.sendMessage(message);
            }

            TextView waterAmountText = findViewById(R.id.waterAmountText);
            String todayAmountText = String.valueOf(myApp.getTodayAmount()) + "mL";
            waterAmountText.setText(todayAmountText);

            int percentage = (int) ((float) myApp.getTodayAmount() / myApp.getGoalAmount() * 100);
            TextView amountPercent = findViewById(R.id.amountPercent);
            String percent = String.valueOf(percentage) + "%";
            amountPercent.setText(percent);

            String now = "현재 텀블러 측정값 : " + String.valueOf(myApp.getBeforeAmount());


            Toast.makeText(getApplicationContext(), "add water!", Toast.LENGTH_SHORT).show();

        });

        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {                             // 밝은 테마에서 스위치를 눌렀을 시에
            if (isChecked) {                                                                        // if ~ else 문을 이용해서 지금 상태를 확인 후 테마를 변환해준다.
                switchToDarkTheme();
            } else {
                switchToLightTheme();
            }
        });

        switch2.setOnCheckedChangeListener((buttonView, isChecked) -> {                             // 어두운 테마에서 스위치를 눌렀을 시에
            if (isChecked) {
                switchToDarkTheme();
            } else {
                switchToLightTheme();
            }
        });

        buttonSetting.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);       // intent 를 사용해서 원하는 페이지로 넘어간다.
            startActivity(intent);
        });

        statisticsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Stat.class);
            startActivity(intent);
        });

        bluetoothIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BluetoothConnect.class);
            startActivity(intent);
        });

        reloadIcon.setOnClickListener(v -> {
            // bluetoothService.sendData("B");

            Toast.makeText(getApplicationContext(), "Reloaded successfully!", Toast.LENGTH_SHORT).show();

            if (bluetoothService != null) {
                bluetoothService.readData();
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
            }
        });

        informationButton.setOnClickListener(v -> showInformationPopup());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void resetAlarm(MainActivity mainActivity) {
    }

    private void setMainTextVeiw(int todayAmount, int goalAmount, int beforeAmount) {
        // activity_main.xml의 TextView에 연결해서 퍼센트 계산
        int percentage = (int) ((float) todayAmount / goalAmount * 100);
        TextView amountPercent = findViewById(R.id.amountPercent);
        String percent = String.valueOf(percentage) + "%";
        amountPercent.setText(percent);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_PERMISSIONS);
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_PERMISSIONS);
            }
        }
    }

    private void setInitialTheme() {
        lightThemeLayout.setVisibility(View.VISIBLE);
        darkThemeLayout.setVisibility(View.GONE);
        switch1.setChecked(false);
        switch2.setChecked(false);
        switch2.setChecked(false);
        updateIconsForLightTheme();
    }

    private void switchToDarkTheme() {
        lightThemeLayout.setVisibility(View.GONE);
        darkThemeLayout.setVisibility(View.VISIBLE);
        switch1.setChecked(true);
        switch2.setChecked(true);
        updateIconsForDarkTheme();
    }

    private void switchToLightTheme() {
        lightThemeLayout.setVisibility(View.VISIBLE);
        darkThemeLayout.setVisibility(View.GONE);
        switch1.setChecked(false);
        switch2.setChecked(false);
        updateIconsForLightTheme();
    }

    private void updateIconsForLightTheme() {                                                       // 라이트 모드
        buttonSetting.setImageResource(R.drawable.settings);
        statisticsIcon.setImageResource(R.drawable.chart);
        bluetoothIcon.setImageResource(R.drawable.bluetooth);
        reloadIcon.setImageResource(R.drawable.reload);

        nowAmount.setTextColor(Color.parseColor("#194569"));
        waterAmountText.setTextColor(Color.parseColor("#194569"));
        farText.setTextColor(Color.parseColor("#5f84a2"));
        soFarText.setTextColor(Color.parseColor("#5f84a2"));

        settingTextView.setTextColor(Color.parseColor("#194569"));
        bluetoothTextView.setTextColor(Color.parseColor("#194569"));
        statsTextView.setTextColor(Color.parseColor("#194569"));
        refreshTextView.setTextColor(Color.parseColor("#194569"));
    }

    private void updateIconsForDarkTheme() {                                                        // 다크 모드
        buttonSetting.setImageResource(R.drawable.settings_w);
        statisticsIcon.setImageResource(R.drawable.chart_w);
        bluetoothIcon.setImageResource(R.drawable.bluetooth_w);
        reloadIcon.setImageResource(R.drawable.reload_w);

        nowAmount.setTextColor(Color.parseColor("#cadeed"));                               // 아이콘 밑에 글자 색상
        waterAmountText.setTextColor(Color.parseColor("#cadeed"));
        farText.setTextColor(Color.parseColor("#91aec4"));
        soFarText.setTextColor(Color.parseColor("#91aec4"));

        settingTextView.setTextColor(Color.parseColor("#cadeed"));                         //물 양 표기 색상
        bluetoothTextView.setTextColor(Color.parseColor("#cadeed"));
        statsTextView.setTextColor(Color.parseColor("#cadeed"));
        refreshTextView.setTextColor(Color.parseColor("#cadeed"));
    }

    private void showInformationPopup() {//하이루 정보 팝업
        Dialog dialog = new Dialog(MainActivity.this, R.style.RoundedDialog);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("GOALATTAINMENT_CHANNEL_ID", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "GOALATTAINMENT_CHANNEL_ID")
                .setSmallIcon(R.drawable.hirue)
                .setContentTitle("목표 달성 알림")
                .setContentText("축하합니다~!! 일일 목표 달성했습니다!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    public static void resetAlarm(Context context) {
        AlarmManager resetAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent resetIntent = new Intent(context, Initialize.class);
        PendingIntent resetSender = PendingIntent.getBroadcast(context, 0, resetIntent, PendingIntent.FLAG_IMMUTABLE);

        // 자정 시간
        Calendar resetCal = Calendar.getInstance();
        resetCal.setTimeInMillis(System.currentTimeMillis());
        resetCal.set(Calendar.HOUR_OF_DAY, 0);
        resetCal.set(Calendar.MINUTE, 0);
        resetCal.set(Calendar.SECOND, 0);

        //다음날 0시에 맞추기 위해 24시간을 뜻하는 상수인 AlarmManager.INTERVAL_DAY를 더해줌.
        resetAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, resetCal.getTimeInMillis()
                + AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, resetSender);


        SimpleDateFormat format1 = new SimpleDateFormat("MM/dd kk:mm:ss");
        String setResetTime = format1.format(new Date(resetCal.getTimeInMillis() + AlarmManager.INTERVAL_DAY));

        Log.d("resetAlarm", "ResetHour : " + setResetTime);
    }
}

// 2024521016 이동찬
