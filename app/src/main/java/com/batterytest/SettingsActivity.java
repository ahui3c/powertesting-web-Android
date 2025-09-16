package com.batterytest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    
    private EditText etUrl1, etUrl2, etUrl3, etUrl4, etUrl5;
    private SeekBar sbScrollSpeed, sbTestDuration, sbStayTime;
    private TextView tvScrollSpeed, tvTestDuration, tvStayTime, tvAuthorUrl;
    private Button btn30Min, btn60Min;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        sharedPreferences = getSharedPreferences("BatteryTestPrefs", MODE_PRIVATE);
        
        initViews();
        loadSettings();
        setupListeners();
    }
    
    private void initViews() {
        etUrl1 = findViewById(R.id.etUrl1);
        etUrl2 = findViewById(R.id.etUrl2);
        etUrl3 = findViewById(R.id.etUrl3);
        etUrl4 = findViewById(R.id.etUrl4);
        etUrl5 = findViewById(R.id.etUrl5);
        
        sbScrollSpeed = findViewById(R.id.seekBarScrollSpeed);
        sbTestDuration = findViewById(R.id.sbTestDuration);
        sbStayTime = findViewById(R.id.seekBarStayTime);
        
        tvScrollSpeed = findViewById(R.id.tvScrollSpeedValue);
        tvTestDuration = findViewById(R.id.tvTestDuration);
        tvStayTime = findViewById(R.id.tvStayTimeValue);
        tvAuthorUrl = findViewById(R.id.tvAuthorUrl);
        
        // 快速選擇按鈕暫時註解，因為佈局檔案中沒有這些ID
        // btn30Min = findViewById(R.id.btnQuick30);
        // btn60Min = findViewById(R.id.btnQuick60);
    }
    
    private void loadSettings() {
        // 載入網址設定
        etUrl1.setText(sharedPreferences.getString("url_1", "https://ahui3c.com"));
        etUrl2.setText(sharedPreferences.getString("url_2", "https://www.pchome.com.tw"));
        etUrl3.setText(sharedPreferences.getString("url_3", "https://m.mobile01.com"));
        etUrl4.setText(sharedPreferences.getString("url_4", "https://lpcomment.com/"));
        etUrl5.setText(sharedPreferences.getString("url_5", "https://www.toy-people.com/"));
        
        // 載入滑動速度設定
        int scrollSpeed = sharedPreferences.getInt("scroll_speed", 6);
        sbScrollSpeed.setProgress(scrollSpeed - 1);
        updateScrollSpeedText(scrollSpeed);
        
        // 載入測試時間設定
        int testDuration = sharedPreferences.getInt("test_duration", 30);
        sbTestDuration.setProgress(testDuration - 1);
        updateTestDurationText(testDuration);
        
        // 載入停留時間設定 (預設2.0秒，對應progress=10)
        float stayTime = sharedPreferences.getFloat("stay_time", 2.0f);
        int progress = (int)((stayTime - 0.5f) * 10); // 0.5-4.5秒對應0-40
        sbStayTime.setProgress(progress);
        updateStayTimeText(stayTime);
    }
    
    private void setupListeners() {
        // 快速選擇按鈕暫時註解
        /*
        btn30Min.setOnClickListener(v -> {
            sbTestDuration.setProgress(29); // 30分鐘
            updateTestDurationText(30);
            saveSettings();
        });
        
        btn60Min.setOnClickListener(v -> {
            sbTestDuration.setProgress(59); // 60分鐘
            updateTestDurationText(60);
            saveSettings();
        });
        */
        
        // 滑動速度設定
        sbScrollSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int speed = progress + 1;
                    updateScrollSpeedText(speed);
                    saveSettings();
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 測試時間設定
        sbTestDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int duration = progress + 1;
                    updateTestDurationText(duration);
                    saveSettings();
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 停留時間設定
        sbStayTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float stayTime = 0.5f + (progress * 0.1f); // 0.5-4.5秒
                    updateStayTimeText(stayTime);
                    saveSettings();
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 網址輸入框自動儲存
        etUrl1.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) saveSettings(); });
        etUrl2.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) saveSettings(); });
        etUrl3.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) saveSettings(); });
        etUrl4.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) saveSettings(); });
        etUrl5.setOnFocusChangeListener((v, hasFocus) -> { if (!hasFocus) saveSettings(); });
        
        // 作者網址點擊
        tvAuthorUrl.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ahui3.com"));
            startActivity(intent);
        });
    }
    
    private void updateScrollSpeedText(int speed) {
        String speedText;
        if (speed <= 2) speedText = "很慢";
        else if (speed <= 4) speedText = "慢";
        else if (speed <= 6) speedText = "正常";
        else if (speed <= 8) speedText = "快";
        else speedText = "很快";
        
        tvScrollSpeed.setText("滑動速度: " + speed + " (" + speedText + ")");
    }
    
    private void updateTestDurationText(int duration) {
        String durationText;
        if (duration <= 15) durationText = "短期";
        else if (duration <= 45) durationText = "中期";
        else if (duration <= 90) durationText = "長期";
        else durationText = "超長";
        
        tvTestDuration.setText("測試時間: " + duration + " 分鐘 (" + durationText + ")");
    }
    
    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        // 儲存網址設定
        editor.putString("url_1", etUrl1.getText().toString().trim());
        editor.putString("url_2", etUrl2.getText().toString().trim());
        editor.putString("url_3", etUrl3.getText().toString().trim());
        editor.putString("url_4", etUrl4.getText().toString().trim());
        editor.putString("url_5", etUrl5.getText().toString().trim());
        
        // 儲存滑動速度設定
        editor.putInt("scroll_speed", sbScrollSpeed.getProgress() + 1);
        
        // 儲存測試時間設定
        editor.putInt("test_duration", sbTestDuration.getProgress() + 1);
        
        // 儲存停留時間設定
        float stayTime = 0.5f + (sbStayTime.getProgress() * 0.1f);
        editor.putFloat("stay_time", stayTime);
        
        editor.apply();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveSettings(); // 離開頁面時自動儲存
    }
    
    private void updateStayTimeText(float stayTime) {
        tvStayTime.setText(String.format("停留時間: %.1f秒", stayTime));
    }
}
