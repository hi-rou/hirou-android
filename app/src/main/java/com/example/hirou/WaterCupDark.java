package com.example.hirou;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class WaterCupDark extends View {
    private Paint paint;
    private int goalAmount = 2000; // 목표 물 섭취량 (ml)
    private int currentAmount = 0; // 현재 물 섭취량 (ml)

    public WaterCupDark(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaterCupDark(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(100); // 텍스트 크기
        paint.setTextAlign(Paint.Align.CENTER); // 텍스트 정렬
        setLayerType(LAYER_TYPE_SOFTWARE, null); // BlurMaskFilter 사용을 위해 소프트웨어 레이어 유형 설정
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float startHeightAdjustment = height * 0.1f; // 시작점을 아래쪽으로 조정

        // 배경 그라데이션
        LinearGradient backgroundGradient = new LinearGradient(0, 0, 0, height,
                new int[]{Color.parseColor("#194569"), Color.parseColor("#FF000000"), Color.parseColor("#FF000000")},
                new float[]{0, 0.5f, 1}, Shader.TileMode.CLAMP);
        paint.setShader(backgroundGradient);

        // 배경 그라데이션 그리기
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paint);

        // 물방울 경로
        Path dropPath = new Path();
        dropPath.moveTo(width / 2f, (height * 0.2f) + startHeightAdjustment);
        dropPath.quadTo(width * 0.1f, (height * 0.6f) + startHeightAdjustment, width / 2f, (height * 0.6f) + startHeightAdjustment);
        dropPath.quadTo(width * 0.9f, (height * 0.6f) + startHeightAdjustment, width / 2f, (height * 0.2f) + startHeightAdjustment);
        dropPath.close();

        // 그림자 페인트
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.parseColor("#44000000")); // 투명 블랙 그림자
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL)); // 그림자에 블러 효과 설정

        // 그림자 그리기
        canvas.save();
        canvas.translate(15, 35); // 그림자를 오른쪽 아래로 이동
        canvas.drawPath(dropPath, shadowPaint);
        canvas.restore();

        // 물방울 그라데이션
        LinearGradient dropGradient = new LinearGradient(0, 0, 0, height,
                new int[]{Color.parseColor("#dbecf4"), Color.parseColor("#cadeed"), Color.parseColor("#5f84a2")},
                new float[]{0, 0.6f, 1}, Shader.TileMode.CLAMP);
        paint.setShader(dropGradient);
        paint.setMaskFilter(null); // 블러 효과 제거

        // 물방울 채우기
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(dropPath, paint);

        // 물방울에 하이라이트 추가
        RadialGradient highlightGradient = new RadialGradient(width / 2.05f, (height * 0.3f) + startHeightAdjustment,
                width / 3f, new int[]{Color.WHITE, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        paint.setShader(highlightGradient);
        canvas.drawPath(dropPath, paint);

        // 클리핑 영역 설정
        canvas.save();
        canvas.clipPath(dropPath);

        // 물 채우기 경로 설정
        Path waterPath = new Path();
        waterPath.moveTo(width * 0.1f, (height * 0.6f) + startHeightAdjustment);
        waterPath.quadTo(width * 0.25f, (height * 0.5f) + startHeightAdjustment, width * 0.5f, (height * 0.55f) + startHeightAdjustment);
        waterPath.quadTo(width * 0.75f, (height * 0.6f) + startHeightAdjustment, width * 0.9f, (height * 0.5f) + startHeightAdjustment);
        waterPath.lineTo(width * 0.9f, (height * 0.6f) + startHeightAdjustment);
        waterPath.lineTo(width * 0.1f, (height * 0.6f) + startHeightAdjustment);
        waterPath.close();

        // 물 그라데이션
        LinearGradient waterGradient = new LinearGradient(0, (height * 0.55f) + startHeightAdjustment, 0, (height * 0.6f) + startHeightAdjustment,
                new int[]{Color.parseColor("#5f84a2"), Color.parseColor("#cadeed")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        paint.setShader(waterGradient);

        // 물 채우기
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(waterPath, paint);

        // 클리핑 영역 제거
        canvas.restore();

        // 텍스트를 흰색 굵은 글씨로 설정
        paint.setShader(null); // 그라디언트를 텍스트에 적용하지 않도록 설정 해제
        paint.setColor(Color.BLACK); // 텍스트 색상을 흰색으로 설정
        paint.setStyle(Paint.Style.FILL); // 스타일을 FILL_AND_STROKE로 설정하여 굵은 글씨 생성
        paint.setStrokeWidth(5); // 글씨 굵기 설정

        // 다음에 다시 paint 객체를 사용할 때 영향을 주지 않도록 설정 초기화
        paint.setStyle(Paint.Style.FILL); // 다시 FILL로 설정
        paint.setStrokeWidth(0); // 글씨 굵기 초기화

        // Draw small water drops
        drawSmallDrop(canvas, width * 0.15f, height * 0.09f + startHeightAdjustment, width * 0.04f);
        drawSmallDrop(canvas, width * 0.20f, height * 0.04f + startHeightAdjustment, width * 0.03f);
    }

    private void drawSmallDrop(Canvas canvas, float cx, float cy, float radius) {
        // Small drop shadow
        Paint smallShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallShadowPaint.setColor(Color.parseColor("#21000000")); // Lighter translucent black
        smallShadowPaint.setStyle(Paint.Style.FILL);
        smallShadowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL)); // Set blur effect

        // Draw small drop shadow
        canvas.save();
        canvas.translate(10, 20); // Offset shadow to the bottom right
        canvas.drawCircle(cx, cy, radius, smallShadowPaint);
        canvas.restore();

        // Small drop gradient
        RadialGradient smallDropGradient = new RadialGradient(cx, cy, radius,
                new int[]{Color.parseColor("#dbecf4"), Color.parseColor("#cadeed"), Color.parseColor("#dbecf4")},
                new float[]{0, 0.6f, 1}, Shader.TileMode.CLAMP);
        paint.setShader(smallDropGradient);

        // Draw small drop
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius, paint);

        // Add highlight to small drop
        RadialGradient smallHighlightGradient = new RadialGradient(cx, cy - radius / 3, radius / 3,
                new int[]{Color.WHITE, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        paint.setShader(smallHighlightGradient);
        canvas.drawCircle(cx, cy, radius, paint);

        // Reset paint settings
        paint.setShader(null);
    }

    // Method to set the current amount of water consumed
    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
        invalidate(); // Redraw view with updated water amount
    }
}
