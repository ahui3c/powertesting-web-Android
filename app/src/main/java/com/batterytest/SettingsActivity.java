package com.batterytest;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    
    private EditText[] etUrls = new EditText[5];
    private SeekBar sbScrollSpeed, sbTestDuration;
    private TextView tvScrollSpeed, tvTestDuration, tvAuthorWebsite;
    private Button btnSave, btnReset;
    
    private SharedPreferences prefs;
    
    // 新的預設值
    private static final String[] DEFAULT_URLS = {
        "https://ahui3c.com",
        "https://www.pchome.com.tw",
        "https://m.mobile01.com",
        "https://lpcomment.com/",
        "https://www.toy-people.com/"
    };
    private static final int DEFAULT_SCROLL_SPEED = 3; // 1-10的範圍，3為預設
    private static final int DEFAULT_TEST_DURATION = 30; // 預設30分鐘
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // 設定標題
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("設定");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        prefs = getSharedPreferences("BatteryTestSettings", MODE_PRIVATE);
        
        initViews();
        loadSettings();
        setupListeners();
    }
    
    private void initViews() {
        etUrls[0] = findViewById(R.id.etUrl1);
        etUrls[1] = findViewById(R.id.etUrl2);
        etUrls[2] = findViewById(R.id.etUrl3);
        etUrls[3] = findViewById(R.id.etUrl4);
        etUrls[4] = findViewById(R.id.etUrl5);
        
        sbScrollSpeed = findViewById(R.id.sbScrollSpeed);
        tvScrollSpeed = findViewById(R.id.tvScrollSpeed);
        sbTestDuration = findViewById(R.id.sbTestDuration);
        tvTestDuration = findViewById(R.id.tvTestDuration);
        tvAuthorWebsite = findViewById(R.id.tvAuthorWebsite);
        btnSave = findViewById(R.id.btnSave);
        btnReset = findViewById(R.id.btnReset);
    }
    
    private void loadSettings() {
        // 載入儲存的網址設定
        for (int i = 0; i < 5; i++) {
            String savedUrl = prefs.getString("url_" + (i + 1), DEFAULT_URLS[i]);
            etUrls[i].setText(savedUrl);
        }
        
        // 載入滑動速度設定
        int savedSpeed = prefs.getInt("scroll_speed", DEFAULT_SCROLL_SPEED);
        sbScrollSpeed.setProgress(savedSpeed - 1); // SeekBar從0開始
        updateScrollSpeedText(savedSpeed);
        
        // 載入測試時間設定
        int savedDuration = prefs.getInt("test_duration", DEFAULT_TEST_DURATION);
        sbTestDuration.setProgress(savedDuration - 1); // SeekBar從0開始，範圍1-120
        updateTestDurationText(savedDuration);
    }
    
    private void setupListeners() {
        // 滑動速度調整
        sbScrollSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int speed = progress + 1; // 轉換為1-10
                updateScrollSpeedText(speed);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 測試時間調整
        sbTestDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration = progress + 1; // 轉換為1-120
                updateTestDurationText(duration);
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        // 儲存按鈕
        btnSave.setOnClickListener(v -> saveSettings());
        
        // 重設按鈕
        btnReset.setOnClickListener(v -> resetSettings());
        
        // 作者網址點擊
        tvAuthorWebsite.setOnClickListener(v -> {
            try {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("https://ahui3.com"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "無法開啟網頁", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateScrollSpeedText(int speed) {
        String speedText;
        if (speed <= 2) {
            speedText = "很慢";
        } else if (speed <= 4) {
            speedText = "慢";
        } else if (speed <= 6) {
            speedText = "正常";
        } else if (speed <= 8) {
            speedText = "快";
        } else {
            speedText = "很快";
        }
        tvScrollSpeed.setText(String.format("滑動速度: %d (%s)", speed, speedText));
    }
    
    private void updateTestDurationText(int duration) {
        String durationText;
        if (duration <= 5) {
            durationText = "短時間";
        } else if (duration <= 15) {
            durationText = "短期";
        } else if (duration <= 45) {
            durationText = "中期";
        } else if (duration <= 90) {
            durationText = "長期";
        } else {
            durationText = "超長期";
        }
        tvTestDuration.setText(String.format("測試時間: %d 分鐘 (%s)", duration, durationText));
    }
    
    private void saveSettings() {
        int speed = sbScrollSpeed.getProgress() + 1;
        int duration = sbTestDuration.getProgress() + 1;
        boolean hasValidUrl = false;
        
        SharedPreferences.Editor editor = prefs.edit();
        
        // 儲存網址設定
        for (int i = 0; i < 5; i++) {
            String url = etUrls[i].getText().toString().trim();
            
            if (!url.isEmpty()) {
                // 確保網址有協議
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                hasValidUrl = true;
            } else {
                // 如果為空，使用預設網址
                url = DEFAULT_URLS[i];
                hasValidUrl = true;
            }
            
            editor.putString("url_" + (i + 1), url);
        }
        
        // 檢查是否至少有一個有效網址
        if (!hasValidUrl) {
            Toast.makeText(this, "請至少輸入一個網址", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 儲存滑動速度和測試時間
        editor.putInt("scroll_speed", speed);
        editor.putInt("test_duration", duration);
        editor.apply();
        
        Toast.makeText(this, "設定已儲存", Toast.LENGTH_SHORT).show();
        finish(); // 返回主頁面
    }
    
    private void resetSettings() {
        // 重設所有網址為預設值
        for (int i = 0; i < 5; i++) {
            etUrls[i].setText(DEFAULT_URLS[i]);
        }
        
        // 重設滑動速度
        sbScrollSpeed.setProgress(DEFAULT_SCROLL_SPEED - 1);
        updateScrollSpeedText(DEFAULT_SCROLL_SPEED);
        
        // 重設測試時間
        sbTestDuration.setProgress(DEFAULT_TEST_DURATION - 1);
        updateTestDurationText(DEFAULT_TEST_DURATION);
        
        Toast.makeText(this, "設定已重設為預設值", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

