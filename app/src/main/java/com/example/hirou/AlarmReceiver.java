package com.example.hirou;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        MyApp myApp = (MyApp) context.getApplicationContext();
        if (myApp.getTodayAmount() < myApp.getGoalAmount()) {

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(context, MainActivity.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setSmallIcon(R.drawable.hirue);

                String channelName = "매일 알람 채널";
                String description = "매일 정해진 시간에 알람";
                int importance = notificationManager.IMPORTANCE_HIGH;

                NotificationChannel channel = new NotificationChannel("default", channelName, importance);
                channel.setDescription(description);

                if (notificationManager != null) {
                    // 노티피케이션 채널을 시스템에 등록
                    notificationManager.createNotificationChannel(channel);
                }
            } else builder.setSmallIcon(R.mipmap.ic_launcher);

            builder.setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())

                    .setTicker("{Time to watch soem cool stuff!}")
                    .setContentTitle("목표치 미달성 알림")
                    .setContentText("오늘 섭취한 양이 목표치에 도달하지 못했습니다 ㅠㅠ \n얼른 물을 마셔주세요~~!!")
                    .setContentInfo("INFO")
                    .setContentIntent(pendingIntent);

            if (notificationManager != null) {
                // 노티피케이션 동작 시킴
                notificationManager.notify(1234, builder.build());

                Calendar nextNotifyTime = Calendar.getInstance();

                // 내일 같은 시간으로 알림 시간 설정
                nextNotifyTime.add(Calendar.DATE, 1);

                // Preference에 설정한 값 저장
                SharedPreferences.Editor editor = context.getSharedPreferences("DailyAlarm", Context.MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
                editor.apply();

           /* Date currentDateTime = nextNotifyTime.getTime(); // 다음 알림
            String date_text = new SimpleDateFormat("내일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
            Toast.makeText(context.getApplicationContext(), "다음 알림은 " + date_text + "입니다!", Toast.LENGTH_SHORT).show();*/
            }
        }
    }
}
