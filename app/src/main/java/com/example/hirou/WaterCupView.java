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

public class WaterCupView extends View {
    private Paint paint;

    public WaterCupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaterCupView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(80); // 텍스트 크기
        paint.setTextAlign(Paint.Align.CENTER); // 텍스트 정렬
        setLayerType(LAYER_TYPE_SOFTWARE, null); // BlurMaskFilter 사용을 위해 소프트웨어 레이어 타입 설정
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        MyApp myApp = (MyApp) getContext().getApplicationContext();

        int width = getWidth();
        int height = getHeight();
        float startHeightAdjustment = height * 0.1f; // 시작점과 바닥부분을 아래로 내리는 비율

        // 배경 그라디언트 설정
        LinearGradient backgroundGradient = new LinearGradient(0, 0, 0, height,
                new int[]{Color.parseColor("#dbecf4"), Color.parseColor("#FFFFFFFF"), Color.parseColor("#FFFFFFFF")},
                new float[]{0, 0.5f, 1}, Shader.TileMode.CLAMP);
        paint.setShader(backgroundGradient);

        // 배경 그라디언트 그리기
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, paint);

        // 물방울 경로 설정
        Path dropPath = new Path();
        dropPath.moveTo(width / 2f, (height * 0.2f) + startHeightAdjustment);
        dropPath.quadTo(width * 0.1f, (height * 0.6f) + startHeightAdjustment, width / 2f, (height * 0.6f) + startHeightAdjustment);
        dropPath.quadTo(width * 0.9f, (height * 0.6f) + startHeightAdjustment, width / 2f, (height * 0.2f) + startHeightAdjustment);
        dropPath.close();

        // 그림자 페인트 설정
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.parseColor("#44000000")); // 더 밝은 반투명 검정색 그림자
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL)); // 그림자를 더 흐리게 설정

        // 그림자를 아래로 치우치게 하기 위해 캔버스 변환
        canvas.save();
        canvas.translate(15, 35); // 그림자를 오른쪽 아래로 치우치게 설정
        canvas.drawPath(dropPath, shadowPaint);
        canvas.restore();

        // 물방울 그라디언트 설정
        LinearGradient dropGradient = new LinearGradient(0, 0, 0, height,
                new int[]{Color.parseColor("#dbecf4"), Color.parseColor("#cadeed"), Color.parseColor("#5f84a2")},
                new float[]{0, 0.6f, 1}, Shader.TileMode.CLAMP);
        paint.setShader(dropGradient);
        paint.setMaskFilter(null); // 그림자 흐리기 효과 제거

        // 물방울 내부 채우기
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(dropPath, paint);

        // 물방울 하이라이트 추가
        RadialGradient highlightGradient = new RadialGradient(width / 2.05f, (height * 0.3f) + startHeightAdjustment,
                width / 3f, new int[]{Color.WHITE, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        paint.setShader(highlightGradient);
        canvas.drawPath(dropPath, paint);

        // 물방울 클리핑 설정
        canvas.save();
        canvas.clipPath(dropPath);

        // 물을 중간 정도 채우기 위한 경로 설정
        Path waterPath = new Path();
        waterPath.moveTo(width * 0.1f, (height * 0.6f) + startHeightAdjustment);
        waterPath.quadTo(width * 0.25f, (height * 0.5f) + startHeightAdjustment, width * 0.5f, (height * 0.55f) + startHeightAdjustment);
        waterPath.quadTo(width * 0.75f, (height * 0.6f) + startHeightAdjustment, width * 0.9f, (height * 0.5f) + startHeightAdjustment);
        waterPath.lineTo(width * 0.9f, (height * 0.6f) + startHeightAdjustment);
        waterPath.lineTo(width * 0.1f, (height * 0.6f) + startHeightAdjustment);
        waterPath.close();

        // 물의 그라디언트 설정
        LinearGradient waterGradient = new LinearGradient(0, (height * 0.55f) + startHeightAdjustment, 0, (height * 0.6f) + startHeightAdjustment,
                new int[]{Color.parseColor("#5f84a2"), Color.parseColor("#cadeed")},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        paint.setShader(waterGradient);

        // 물 채우기
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(waterPath, paint);

        // 클리핑 영역 해제
        canvas.restore();

        // 텍스트를 흰색 굵은 글씨로 설정
        paint.setShader(null); // 그라디언트를 텍스트에 적용하지 않도록 설정 해제
        paint.setColor(Color.BLACK); // 텍스트 색상을 흰색으로 설정
        paint.setStyle(Paint.Style.FILL); // 스타일을 FILL_AND_STROKE로 설정하여 굵은 글씨 생성
        paint.setStrokeWidth(5); // 글씨 굵기 설정

        // 다음에 다시 paint 객체를 사용할 때 영향을 주지 않도록 설정 초기화
        paint.setStyle(Paint.Style.FILL); // 다시 FILL로 설정
        paint.setStrokeWidth(0); // 글씨 굵기 초기화

        // 옆에 맺혀있는 작은 물방울 그리기
        drawSmallDrop(canvas, width * 0.15f, height * 0.09f + startHeightAdjustment, width * 0.04f);
        drawSmallDrop(canvas, width * 0.20f, height * 0.04f + startHeightAdjustment, width * 0.03f);
    }

    private void drawSmallDrop(Canvas canvas, float cx, float cy, float radius) {
        // 작은 물방울 그림자 설정
        Paint smallShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallShadowPaint.setColor(Color.parseColor("#21000000")); // 더 밝은 반투명 검정색
        smallShadowPaint.setStyle(Paint.Style.FILL);
        smallShadowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL)); // 그림자를 더 흐리게 설정

        // 작은 물방울 그림자 그리기
        canvas.save();
        canvas.translate(10, 20); // 그림자를 살짝 오른쪽 아래로 치우치게 설정
        canvas.drawCircle(cx, cy, radius, smallShadowPaint);
        canvas.restore();

        // 작은 물방울 그라디언트 설정
        RadialGradient smallDropGradient = new RadialGradient(cx, cy, radius,
                new int[]{Color.parseColor("#dbecf4"), Color.parseColor("#cadeed"), Color.parseColor("#dbecf4")},
                new float[]{0, 0.6f, 1}, Shader.TileMode.CLAMP);
        paint.setShader(smallDropGradient);

        // 작은 물방울 그리기
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius, paint);

        // 작은 물방울 하이라이트 추가
        RadialGradient smallHighlightGradient = new RadialGradient(cx, cy - radius / 3, radius / 3,
                new int[]{Color.WHITE, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP);
        paint.setShader(smallHighlightGradient);
        canvas.drawCircle(cx, cy, radius, paint);

        // 설정 초기화
        paint.setShader(null);
    }
}