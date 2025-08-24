package com.group5.safezone.view.livestreaming;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

import android.util.Log;

public class DonateAnimationView extends View {

    private Paint paint;
    private Paint textPaint;
    private Paint emojiPaint;
    
    private String donateText = "";
    private String emoji = "💖";
    private int donateAmount = 0;
    
    private float centerX, centerY;
    private float scale = 0f;
    private float alpha = 0f;
    private float rotation = 0f;
    
    private OnAnimationCompleteListener animationCompleteListener;

    public interface OnAnimationCompleteListener {
        void onAnimationComplete();
    }

    public DonateAnimationView(Context context) {
        super(context);
        init();
    }

    public DonateAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonateAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#FF6B6B"));
        paint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        
        emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emojiPaint.setTextSize(64f);
        emojiPaint.setTextAlign(Paint.Align.CENTER);
        emojiPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
            
            if (donateAmount <= 0) return;
            
            // Vẽ background tròn
            paint.setAlpha((int) (255 * alpha));
            canvas.drawCircle(centerX, centerY, 100 * scale, paint);
            
            // Vẽ emoji
            emojiPaint.setAlpha((int) (255 * alpha));
            canvas.drawText(emoji, centerX, centerY - 20, emojiPaint);
            
            // Vẽ text donate
            textPaint.setAlpha((int) (255 * alpha));
            textPaint.setTextSize(48f * scale);
            canvas.drawText(donateText, centerX, centerY + 60, textPaint);
            
            // Vẽ số tiền
            textPaint.setTextSize(36f * scale);
            canvas.drawText(formatCurrency(donateAmount), centerX, centerY + 100, textPaint);
        } catch (Exception e) {
            Log.e("DonateAnimation", "Error in onDraw: " + e.getMessage());
        }
    }

    public void startDonateAnimation(int amount, String emoji, OnAnimationCompleteListener listener) {
        try {
            this.donateAmount = amount;
            this.emoji = emoji != null ? emoji : "💖";
            this.donateText = "DONATE!";
            this.animationCompleteListener = listener;
            
            // Reset animation values
            scale = 0f;
            alpha = 0f;
            rotation = 0f;
            
            // Animation 1: Scale và fade in
            ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(this, "scale", 0f, 1.2f);
            scaleAnimator.setDuration(500);
            scaleAnimator.setInterpolator(new OvershootInterpolator());
            
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);
            alphaAnimator.setDuration(300);
            
            // Animation 2: Bay ra và fade out
            ObjectAnimator flyOutAnimator = ObjectAnimator.ofFloat(this, "translationY", 0f, -200f);
            flyOutAnimator.setDuration(1000);
            flyOutAnimator.setStartDelay(800);
            
            ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);
            fadeOutAnimator.setDuration(1000);
            fadeOutAnimator.setStartDelay(800);
            
            // Animation 3: Rotation
            ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);
            rotationAnimator.setDuration(1500);
            rotationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            
            // Start animations
            scaleAnimator.start();
            alphaAnimator.start();
            rotationAnimator.start();
            
            // Delay fly out animation
            postDelayed(() -> {
                try {
                    flyOutAnimator.start();
                    fadeOutAnimator.start();
                } catch (Exception e) {
                    Log.e("DonateAnimation", "Error starting fly out animation: " + e.getMessage());
                }
            }, 800);
            
            // Listen for completion
            fadeOutAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    try {
                        if (animationCompleteListener != null) {
                            animationCompleteListener.onAnimationComplete();
                        }
                        // Reset view
                        setTranslationY(0f);
                        setRotation(0f);
                        invalidate();
                    } catch (Exception e) {
                        Log.e("DonateAnimation", "Error in animation completion: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e("DonateAnimation", "Error starting donate animation: " + e.getMessage());
        }
    }

    private String formatCurrency(int amount) {
        try {
            if (amount >= 1000) {
                return String.format("%.0fK", amount / 1000.0);
            }
            return String.valueOf(amount);
        } catch (Exception e) {
            Log.e("DonateAnimation", "Error formatting currency: " + e.getMessage());
            return String.valueOf(amount);
        }
    }

    // Custom property setters for animation
    public void setScale(float scale) {
        this.scale = scale;
        invalidate();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        invalidate();
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        invalidate();
    }
}
