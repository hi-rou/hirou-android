package com.example.hirou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class Initialize extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 애플리케이션 컨텍스트를 사용하여 MyApp 인스턴스를 가져옵니다.
        MyApp myApp = (MyApp) context.getApplicationContext();

        /*Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // 날짜에 따른 다른 로직 실행
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                // 월요일 작업 실행
                myApp.initializeToday(7);
                break;
            case Calendar.TUESDAY:
                // 화요일 작업 실행
                myApp.initializeToday(1);
                break;
            case Calendar.WEDNESDAY:
                // 수요일 작업 실행
                myApp.initializeToday(2);
                break;
            case Calendar.THURSDAY:
                // 목요일 작업 실행
                myApp.initializeToday(3);
                break;
            case Calendar.FRIDAY:
                // 금요일 작업 실행
                myApp.initializeToday(4);
                break;
            case Calendar.SATURDAY:
                // 토요일 작업 실행
                myApp.initializeToday(5);
                break;
            case Calendar.SUNDAY:
                // 일요일 작업 실행
                myApp.initializeToday(6);
                break;
        }*/

        for (int i=1; i<8; i++){
            myApp.setDayValue(i);
            if (i > 7){
                i = 1;
            }
        }

        switch (myApp.getDayValue()){
            case 1:
                myApp.initializeToday(7);
                break;
            case 2:
                myApp.initializeToday(1);
                break;
            case 3:
                myApp.initializeToday(2);
                break;
            case 4:
                myApp.initializeToday(3);
                break;
            case 5:
                myApp.initializeToday(4);
                break;
            case 6:
                myApp.initializeToday(5);
                break;
            case 7:
                myApp.initializeToday(6);
                break;
        }

        // myApp 객체를 사용하여 initializeToday 메소드를 호출합니다.
        // 메소드 이름이 initializeToday라고 가정하면, 올바르게 메소드를 호출하기 위해 메소드 이름 뒤에 괄호를 추가해야 합니다.
        // myApp.initializeToday(1);
    }
}