package com.example.hirou;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyApp extends Application {
    private int todayAmount; // 오늘 마신량
    private int goalAmount; // 목표치
    private int converted; // 블투 값 받아오는 것
    private int num; // 날짜 변수


    @Override
    public void onCreate() {
        super.onCreate();
        // 초기화 코드
        todayAmount = getBeforeAmount();
        goalAmount = getGoalAmount();
    }

    public void savedConverted(int con) {
        converted = con;
    }

    public int getConverted() {
        return converted;
    }

    public int getCurrentAmount(int x) {
        // 아두이노에서 받아오는 값 return
        return x;
    }

    public int getTodayAmount() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedTodayAmount", 0); // 이전 무게값
    }

    public void setTodayAmount(int currentWeight) { // sharedPreferences를 이용해 현재 물 무게 저장하는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedTodayAmount", currentWeight); // 현재 물 무게 sharedPreferences에 저장
        editor.apply();
    }

    public int getBeforeAmount() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedBeforeAmount", 0); // 이전 무게값
    }

    public void setBeforeAmount(int currentWeight) { // sharedPreferences를 이용해 현재 물 무게 저장하는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedBeforeAmount", currentWeight); // 현재 물 무게 sharedPreferences에 저장
        editor.apply();
    }

    public int reloadAmount(int value) { // 새로고침 버튼 - 물을 그냥 마신 경우
        if (value >= 0) {
            int beforeWeight = getBeforeAmount(); // 저장된 값 불러오기
            setBeforeAmount(getCurrentAmount(value)); // 현재 무게 sharedPreferences에 저장

            if (beforeWeight < getCurrentAmount(value)) { // 물 양이 늘어나면 마신 것이 아니니 그냥 오늘 물 값 반환
                return getTodayAmount();
            } else { // 그게 아니라면 줄어든만큼 마신량으로 계산
                setTodayAmount(getTodayAmount() + (beforeWeight - getCurrentAmount(value)));
                return getTodayAmount();
            }
        }
        return 0;
    }

    public int drainAmount(int value) { // 물버림 버튼 - 물을 버리고 새로 따른 경우
        setBeforeAmount(0);

        return getTodayAmount();
    }

    public int drinkAmount(int value) { // 물 채움 버튼 - 물을 다 마시고 새로 물을 따른 경우
        int beforeWeight = getBeforeAmount(); // 저장된 값 불러오기
        setBeforeAmount(getCurrentAmount(value)); // 현재 무게 sharedPreferences에 저장

        setTodayAmount(getTodayAmount() + beforeWeight); // 기존 무게를 먹은 걸로 계산
        return getTodayAmount();
    }

    public void formatTodayAmount() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedTodayAmount", 0); // 현재 물 무게 sharedPreferences에 저장
        editor.apply();
    }

    public void setDayValue(int i){
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedDayValue", i);
        editor.apply();
    }

    public int getDayValue() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedDayValue", 0); // 이전 무게값
    }

    public void addDayValueForMain(){
        if (getDayValueforMain() < 7) {
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("savedDayValueForMain", getDayValueforMain() + 1);
            editor.apply();
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("savedDayValueForMain", 0);
            editor.apply();
        }
    }
    public int getDayValueforMain() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedDayValueForMain", 1); // 이전 무게값
    }


    public void initializeToday(int day) {
        switch (day) {
            case 1:
                setMon(getTodayAmount());
                setTue(0);
                setWed(0);
                setThu(0);
                setFri(0);
                setSat(0);
                setSun(0);
                break;
            case 2:
                setTue(getTodayAmount());
                break;
            case 3:
                setWed(getTodayAmount());
                break;
            case 4:
                setThu(getTodayAmount());
                break;
            case 5:
                setFri(getTodayAmount());
                break;
            case 6:
                setSat(getTodayAmount());
                break;
            case 7:
                setSun(getTodayAmount());
                break;
            default:
                break;
        }
        setTodayAmount(0);
        setBeforeAmount(0);
    }


    public int getGoalAmount() {
        // 값 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedGoalAmount", 0);
    }

    public void setGoalAmount(int goalAmount) { // 사용자가 알림 설정 화면에서 설정한 값을 받아오는 메소드
        // 값 저장하기 - SharedPreferences의 key인 'savedGoalAmount'에 설정 액티비티에서 스피너로 사용자가 입력한 값 저장.
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedGoalAmount", goalAmount);
        editor.apply();
    }

    public int getGoalSpinnerItem() {
        // 값 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedGoalSpinnerItem", 0);
    }

    public void setGoalSpinnerItem(int itemNum) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedGoalSpinnerItem", itemNum);
        editor.apply();
    }

    public String getSelectedTime() {
        // 값 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("DailyAlarm", MODE_PRIVATE);
        return sharedPreferences.getString("selectedTime", "알림을 설정해주세요");
    }

    public void setSelectedTime(String selectedTime) {
        SharedPreferences sharedPreferences = getSharedPreferences("DailyAlarm", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedTime", selectedTime);
        editor.apply();
    }


    // 요일별 컨트롤
    public void setMon(int todayAmount) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedMonAmount", todayAmount);
        editor.apply();
    }

    public void setTue(int todayAmount) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedTueAmount", todayAmount);
        editor.apply();
    }

    public void setWed(int todayAmount) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedWedAmount", todayAmount);
        editor.apply();
    }

    public void setThu(int todayAmount) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedThuAmount", todayAmount);
        editor.apply();
    }

    public void setFri(int todayAmount) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedFriAmount", todayAmount);
        editor.apply();
    }

    public void setSat(int todayAmount) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedSatAmount", todayAmount);
        editor.apply();
    }

    public void setSun(int todayAmount) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("savedSunAmount", todayAmount);
        editor.apply();
    }


    public int getMon() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedMonAmount", 0); // 이전 무게값
    }

    public int getTue() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedTueAmount", 0); // 이전 무게값
    }

    public int getWed() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedWedAmount", 0); // 이전 무게값
    }

    public int getThu() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedThuAmount", 0); // 이전 무게값
    }

    public int getFri() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedFriAmount", 0); // 이전 무게값
    }

    public int getSat() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedSatAmount", 0); // 이전 무게값
    }

    public int getSun() { // 저장된 sharedPreferences를 받아오는 메소드
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sharedPreferences.getInt("savedSunAmount", 0); // 이전 무게값
    }
}