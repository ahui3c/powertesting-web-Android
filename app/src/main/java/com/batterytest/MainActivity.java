package com.batterytest;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private TextView tvBattery, tvStatus, tvTimer, tvCurrentUrl;
    private Button btnStart, btnSettings;
    private boolean isTestRunning = false;
    private Handler handler = new Handler();
    private long startTime = 0;
    
    // 設定相關
    private SharedPreferences prefs;
    private String[] testUrls = new String[5];
    private int scrollSpeed = 3; // 1-10
    private int testDuration = 30; // 測試時間（分鐘）
    
    // 網址循環相關
    private int currentUrlIndex = 0;
    private boolean isAtBottom = false;
    
    // 測試記錄相關
    private int startBatteryLevel = -1;
    private long testStartTime = 0;
    private long testEndTime = 0;
    
    // 新的預設網址
    private static final String[] DEFAULT_URLS = {
        "https://ahui3c.com",
        "https://www.pchome.com.tw",
        "https://m.mobile01.com",
        "https://lpcomment.com/",
        "https://www.toy-people.com/"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 隱藏標題欄
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // 支援高更新率顯示
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                getWindow().getAttributes().preferredDisplayModeId = 0;
                getWindow().getAttributes().preferredRefreshRate = 120.0f;
            } catch (Exception e) {
                // 忽略不支援的裝置
            }
        }
        
        // 保持螢幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences("BatteryTestSettings", MODE_PRIVATE);
        
        initViews();
        loadSettings();
        setupWebView();
        setupButtons();
        
        // 載入第一個網址
        loadCurrentUrl();
        
        // 開始電量監控
        startBatteryMonitoring();
    }
    
    private void initViews() {
        webView = findViewById(R.id.webView);
        tvBattery = findViewById(R.id.tvBattery);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvCurrentUrl = findViewById(R.id.tvCurrentUrl);
        btnStart = findViewById(R.id.btnStart);
        btnSettings = findViewById(R.id.btnSettings);
    }
    
    private void loadSettings() {
        // 載入5組網址設定
        for (int i = 0; i < 5; i++) {
            testUrls[i] = prefs.getString("url_" + (i + 1), DEFAULT_URLS[i]);
        }
        
        scrollSpeed = prefs.getInt("scroll_speed", 3);
        testDuration = prefs.getInt("test_duration", 30);
    }
    
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 頁面載入完成後重設滑動狀態
                isAtBottom = false;
                updateScrollInfo();
                
                // 更新當前網址顯示
                updateCurrentUrlDisplay();
            }
        });
    }
    
    private void setupButtons() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTestRunning) {
                    startTest();
                } else {
                    stopTest();
                }
            }
        });
        
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 重新載入設定
        loadSettings();
        
        // 如果不在測試中，載入當前網址
        if (!isTestRunning) {
            loadCurrentUrl();
        }
        
        // 確保螢幕保持常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 如果不在測試中，可以允許螢幕休眠
        if (!isTestRunning) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    
    private void loadCurrentUrl() {
        String currentUrl = testUrls[currentUrlIndex];
        webView.loadUrl(currentUrl);
        updateCurrentUrlDisplay();
    }
    
    private void updateCurrentUrlDisplay() {
        String urlName = getUrlDisplayName(testUrls[currentUrlIndex]);
        tvCurrentUrl.setText(String.format("%s %d/5", urlName, currentUrlIndex + 1));
    }
    
    private String getUrlDisplayName(String url) {
        if (url.contains("ahui3c.com")) return "阿輝";
        if (url.contains("pchome.com.tw")) return "PChome";
        if (url.contains("mobile01.com")) return "M01";
        if (url.contains("lpcomment.com")) return "LP";
        if (url.contains("toy-people.com")) return "玩具人";
        
        // 提取域名
        try {
            String domain = url.replace("https://", "").replace("http://", "");
            if (domain.contains("/")) {
                domain = domain.substring(0, domain.indexOf("/"));
            }
            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }
            // 取前6個字符
            return domain.length() > 6 ? domain.substring(0, 6) : domain;
        } catch (Exception e) {
            return "網站";
        }
    }
    
    private void startTest() {
        isTestRunning = true;
        startTime = System.currentTimeMillis();
        testStartTime = startTime;
        
        // 記錄開始時的電量
        startBatteryLevel = getCurrentBatteryLevel();
        
        btnStart.setText("停止");
        tvStatus.setText("測試中...");
        
        // 確保螢幕保持常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // 從第一個網址開始
        currentUrlIndex = 0;
        loadCurrentUrl();
        
        // 開始自動滑動
        startAutoScroll();
        
        // 設定測試時間結束的定時器
        handler.postDelayed(() -> {
            if (isTestRunning) {
                stopTestWithResults();
            }
        }, testDuration * 60 * 1000); // 轉換為毫秒
    }
    
    private void stopTest() {
        stopTestWithResults();
    }
    
    private void stopTestWithResults() {
        if (!isTestRunning) return;
        
        isTestRunning = false;
        testEndTime = System.currentTimeMillis();
        btnStart.setText("開始");
        
        // 停止自動滑動
        handler.removeCallbacksAndMessages(null);
        
        // 可以允許螢幕休眠
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // 顯示測試結果
        showTestResults();
    }
    
    private void showTestResults() {
        int endBatteryLevel = getCurrentBatteryLevel();
        long testDurationMs = testEndTime - testStartTime;
        
        // 計算測試時間
        long testMinutes = testDurationMs / (60 * 1000);
        long testSeconds = (testDurationMs % (60 * 1000)) / 1000;
        
        // 格式化開始和結束時間
        String startTimeStr = formatTime(testStartTime);
        String endTimeStr = formatTime(testEndTime);
        
        // 計算電量消耗
        int batteryDrop = startBatteryLevel - endBatteryLevel;
        
        // 獲取手機型號資訊
        String deviceModel = getDeviceInfo();
        
        // 建立結果訊息
        StringBuilder result = new StringBuilder();
        result.append("測電力_網頁 測試結果報告\n\n");
        result.append("裝置資訊:\n");
        result.append(String.format("手機型號: %s\n\n", deviceModel));
        result.append("測試時間:\n");
        result.append(String.format("開始: %s\n", startTimeStr));
        result.append(String.format("結束: %s\n", endTimeStr));
        result.append(String.format("總時長: %d分%d秒\n\n", testMinutes, testSeconds));
        result.append("電量變化:\n");
        result.append(String.format("開始電量: %d%%\n", startBatteryLevel));
        result.append(String.format("結束電量: %d%%\n", endBatteryLevel));
        result.append(String.format("消耗電量: %d%%\n\n", batteryDrop));
        
        if (batteryDrop > 0) {
            double hourlyConsumption = (double) batteryDrop / (testMinutes / 60.0);
            result.append(String.format("每小時消耗: %.1f%%", hourlyConsumption));
        }
        
        String resultText = result.toString();
        
        // 顯示結果對話框
        new android.app.AlertDialog.Builder(this)
            .setTitle("測試完成")
            .setMessage(resultText)
            .setPositiveButton("確定", null)
            .setNeutralButton("重新測試", (dialog, which) -> {
                // 重新開始測試
                startTest();
            })
            .setNegativeButton("複製結果", (dialog, which) -> {
                // 複製結果到剪貼簿
                copyToClipboard(resultText);
            })
            .show();
        
        tvStatus.setText("測試完成");
    }
    
    private String getDeviceInfo() {
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        String version = android.os.Build.VERSION.RELEASE;
        
        // 格式化廠商名稱
        String formattedManufacturer = manufacturer.substring(0, 1).toUpperCase() + 
                                     manufacturer.substring(1).toLowerCase();
        
        return String.format("%s %s (Android %s)", formattedManufacturer, model, version);
    }
    
    private void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard = 
            (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("測試結果", text);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "測試結果已複製到剪貼簿", Toast.LENGTH_SHORT).show();
    }
    
    private String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
    
    private int getCurrentBatteryLevel() {
        android.content.IntentFilter filter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
        android.content.Intent batteryStatus = registerReceiver(null, filter);
        
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
            
            if (level != -1 && scale != -1) {
                return (int) ((level / (float) scale) * 100);
            }
        }
        return -1;
    }
    
    private void startAutoScroll() {
        // 根據速度計算延遲時間 (速度越快延遲越短)
        int baseDelay = 1000; // 基礎延遲1秒
        int delay = Math.max(200, baseDelay - (scrollSpeed - 1) * 80); // 200ms到1000ms
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isTestRunning) {
                    performNaturalScroll();
                    updateTimer();
                    
                    // 繼續滑動
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);
    }
    
    private void performNaturalScroll() {
        // 更新滑動資訊
        updateScrollInfo();
        
        // 檢查是否到達底部
        if (isAtBottom) {
            // 切換到下一個網址
            switchToNextUrl();
            return;
        }
        
        // 模擬自然的手指滑動
        simulateNaturalScroll();
    }
    
    private void switchToNextUrl() {
        // 移動到下一個網址
        currentUrlIndex = (currentUrlIndex + 1) % 5; // 循環回到第一個
        
        String urlName = getUrlDisplayName(testUrls[currentUrlIndex]);
        tvStatus.setText(String.format("切換到%s", urlName));
        
        // 載入下一個網址
        loadCurrentUrl();
        
        // 重設滑動狀態
        isAtBottom = false;
        
        // 等待頁面載入後繼續測試
        handler.postDelayed(() -> {
            if (isTestRunning) {
                tvStatus.setText("測試中...");
            }
        }, 3000); // 等待3秒讓頁面載入
    }
    
    private void simulateNaturalScroll() {
        // 計算滑動距離 (根據速度調整)
        int baseScrollDistance = 150;
        int scrollDistance = baseScrollDistance + (scrollSpeed - 1) * 50; // 150-600像素
        
        // 模擬手指滑動的動作事件
        long downTime = System.currentTimeMillis();
        
        // 開始觸摸
        MotionEvent downEvent = MotionEvent.obtain(
            downTime, downTime, MotionEvent.ACTION_DOWN,
            webView.getWidth() / 2f, webView.getHeight() / 2f, 0
        );
        
        // 滑動過程 (分多個步驟模擬平滑滑動)
        int steps = 10;
        float stepDistance = scrollDistance / (float) steps;
        
        for (int i = 0; i <= steps; i++) {
            long eventTime = downTime + (i * 20); // 每20ms一個步驟
            float y = webView.getHeight() / 2f - (i * stepDistance);
            
            int action = (i == 0) ? MotionEvent.ACTION_DOWN : 
                        (i == steps) ? MotionEvent.ACTION_UP : MotionEvent.ACTION_MOVE;
            
            MotionEvent moveEvent = MotionEvent.obtain(
                downTime, eventTime, action,
                webView.getWidth() / 2f, y, 0
            );
            
            // 延遲發送事件以模擬真實滑動
            final MotionEvent finalEvent = moveEvent;
            handler.postDelayed(() -> {
                webView.dispatchTouchEvent(finalEvent);
                finalEvent.recycle();
            }, i * 20);
        }
        
        downEvent.recycle();
    }
    
    private void updateScrollInfo() {
        // 使用JavaScript獲取滑動資訊
        webView.evaluateJavascript(
            "(function() { " +
            "  var scrollTop = window.pageYOffset || document.documentElement.scrollTop; " +
            "  var scrollHeight = document.documentElement.scrollHeight; " +
            "  var clientHeight = document.documentElement.clientHeight; " +
            "  return JSON.stringify({scrollTop: scrollTop, scrollHeight: scrollHeight, clientHeight: clientHeight}); " +
            "})();",
            value -> {
                try {
                    if (value != null && !value.equals("null")) {
                        // 檢查是否接近底部 (距離底部小於200像素)
                        webView.evaluateJavascript(
                            "window.pageYOffset + window.innerHeight >= document.documentElement.scrollHeight - 200",
                            result -> {
                                isAtBottom = "true".equals(result);
                            }
                        );
                    }
                } catch (Exception e) {
                    // 忽略解析錯誤
                }
            }
        );
    }
    
    private void updateTimer() {
        if (isTestRunning && startTime > 0) {
            long elapsed = System.currentTimeMillis() - startTime;
            long seconds = elapsed / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }
    
    private void startBatteryMonitoring() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateBatteryLevel();
                handler.postDelayed(this, 5000); // 每5秒更新一次
            }
        });
    }
    
    private void updateBatteryLevel() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);
        
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            
            if (level != -1 && scale != -1) {
                int batteryPct = (int) ((level / (float) scale) * 100);
                tvBattery.setText(String.format("電量: %d%%", batteryPct));
            }
        }
    }
}

