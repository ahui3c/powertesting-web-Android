package com.batterytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private TextView tvBattery, tvCurrentUrl, tvTimer, tvStatus;
    private Button btnStart, btnSettings, btnHistory;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler testHandler = new Handler(Looper.getMainLooper());
    private Runnable scrollRunnable;
    private Runnable testTimeoutRunnable;
    
    private boolean isTestRunning = false;
    private long testStartTime;
    private int startBatteryLevel;
    private int currentUrlIndex = 0;
    private String[] testUrls = new String[5];
    private int scrollSpeed = 6;
    private int testDurationMinutes = 60;
    private float stayTimeSeconds = 1.0f;
    
    private BroadcastReceiver batteryReceiver;
    private Vibrator vibrator;
    private ToneGenerator toneGenerator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupWebView();
        loadSettings();
        setupBatteryReceiver();
        initFeedback();
        setupButtonListeners();
        updateBatteryLevel();
        
        // 支援高更新率
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().getAttributes().preferredDisplayModeId = 0;
        }
    }
    
    private void initViews() {
        webView = findViewById(R.id.webView);
        tvBattery = findViewById(R.id.tvBattery);
        tvCurrentUrl = findViewById(R.id.tvCurrentUrl);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        btnStart = findViewById(R.id.btnStart);
        btnSettings = findViewById(R.id.btnSettings);
        btnHistory = findViewById(R.id.btnHistory);
    }
    
    private void initFeedback() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        } catch (RuntimeException e) {
            toneGenerator = null;
        }
    }
    
    private void playFeedback() {
        // 震動回饋
        if (vibrator != null && vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(100);
            }
        }
        
        // 聲音回饋
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
        }
    }
    
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        
        // 啟用硬體加速和高效能設定
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setEnableSmoothTransition(true);
        
        // 啟用WebView的硬體加速層
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        
        // 設定WebView的滑動效能
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        webView.setNestedScrollingEnabled(true);
        
        // 啟用WebView的觸控優化
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isTestRunning) {
                    handler.postDelayed(() -> {
                        if (isTestRunning) {
                            startScrolling();
                        }
                    }, 3000);
                }
            }
        });
    }
    
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("BatteryTestPrefs", MODE_PRIVATE);
        
        // 載入網址設定
        testUrls[0] = prefs.getString("url_1", "https://ahui3c.com");
        testUrls[1] = prefs.getString("url_2", "https://www.pchome.com.tw");
        testUrls[2] = prefs.getString("url_3", "https://m.mobile01.com");
        testUrls[3] = prefs.getString("url_4", "https://lpcomment.com/");
        testUrls[4] = prefs.getString("url_5", "https://www.toy-people.com/");
        
        // 載入滑動速度設定
        scrollSpeed = prefs.getInt("scroll_speed", 6);
        
        // 載入測試時間設定
        testDurationMinutes = prefs.getInt("test_duration", 60);
        
        // 載入停留時間設定
        stayTimeSeconds = prefs.getFloat("stay_time", 1.0f);
    }
    
    private void setupBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBatteryLevel();
            }
        };
        
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }
    
    private void updateBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level / (float) scale) * 100);
            
            tvBattery.setText("電量: " + batteryPct + "%");
            
            if (isTestRunning) {
                tvBattery.setTextColor(Color.RED);
            } else {
                tvBattery.setTextColor(Color.parseColor("#4CAF50"));
            }
        }
    }
    
    private void setupButtonListeners() {
        btnStart.setOnClickListener(v -> {
            playFeedback();
            if (isTestRunning) {
                stopTest();
            } else {
                startTest();
            }
        });
        
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, TestHistoryActivity.class);
            startActivity(intent);
        });
    }
    
    private void startTest() {
        loadSettings(); // 重新載入設定
        
        isTestRunning = true;
        testStartTime = System.currentTimeMillis();
        startBatteryLevel = getCurrentBatteryLevel();
        currentUrlIndex = 0;
        
        // 更新UI
        btnStart.setText("停止");
        btnStart.setBackgroundTintList(getColorStateList(android.R.color.holo_red_dark));
        tvTimer.setTextColor(Color.RED);
        tvStatus.setText("測試進行中...");
        
        // 防止螢幕休眠
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // 載入第一個網址
        loadNextUrl();
        
        // 開始計時器
        startTimer();
        
        // 設定測試結束時間
        testTimeoutRunnable = () -> {
            if (isTestRunning) {
                stopTestWithResults();
            }
        };
        testHandler.postDelayed(testTimeoutRunnable, testDurationMinutes * 60 * 1000);
        
        Toast.makeText(this, "測試開始 - " + testDurationMinutes + "分鐘", Toast.LENGTH_SHORT).show();
    }
    
    private void stopTest() {
        if (!isTestRunning) return;
        
        // 記錄結束時間和電量
        long testEndTime = System.currentTimeMillis();
        int endBatteryLevel = getCurrentBatteryLevel();
        long testDuration = testEndTime - testStartTime;
        
        playFeedback();
        
        isTestRunning = false;
        
        // 停止所有任務
        handler.removeCallbacks(scrollRunnable);
        testHandler.removeCallbacks(testTimeoutRunnable);
        
        // 更新UI
        btnStart.setText("開始");
        btnStart.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_bright));
        tvTimer.setTextColor(Color.parseColor("#FF9800"));
        tvStatus.setText("測試完成");
        
        // 恢復螢幕設定
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // 顯示測試結果（手動停止）
        showTestResults(testDuration, startBatteryLevel, endBatteryLevel, false);
    }
    
    private void stopTestWithResults() {
        if (!isTestRunning) return;
        
        playFeedback();
        
        long testEndTime = System.currentTimeMillis();
        int endBatteryLevel = getCurrentBatteryLevel();
        long testDuration = testEndTime - testStartTime;
        
        isTestRunning = false;
        
        // 停止所有任務
        handler.removeCallbacks(scrollRunnable);
        testHandler.removeCallbacks(testTimeoutRunnable);
        
        // 更新UI
        btnStart.setText("開始");
        btnStart.setBackgroundTintList(getColorStateList(android.R.color.holo_blue_bright));
        tvTimer.setTextColor(Color.parseColor("#FF9800"));
        tvStatus.setText("測試完成");
        
        // 恢復螢幕設定
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // 顯示測試結果（時間到自動停止）
        showTestResults(testDuration, startBatteryLevel, endBatteryLevel, true);
    }
    
    private void showTestResults(long testDuration, int startBattery, int endBattery, boolean isAutoStop) {
        String deviceModel = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        String androidVersion = "Android " + android.os.Build.VERSION.RELEASE;
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String startTime = sdf.format(new Date(testStartTime));
        String endTime = sdf.format(new Date(testStartTime + testDuration));
        
        long minutes = testDuration / (1000 * 60);
        long seconds = (testDuration % (1000 * 60)) / 1000;
        
        int batteryConsumed = startBattery - endBattery;
        double hourlyConsumption = (batteryConsumed / (testDuration / (1000.0 * 60 * 60)));
        
        String stopReason = isAutoStop ? "時間到自動停止" : "手動停止";
        
        String result = String.format(Locale.getDefault(),
            "測電力_網頁 測試結果報告\n\n" +
            "裝置資訊:\n" +
            "手機型號: %s (%s)\n\n" +
            "測試時間:\n" +
            "開始: %s\n" +
            "結束: %s\n" +
            "總時長: %d分%d秒\n" +
            "停止原因: %s\n\n" +
            "電量變化:\n" +
            "開始電量: %d%%\n" +
            "結束電量: %d%%\n" +
            "消耗電量: %d%%\n\n" +
            "每小時消耗: %.1f%%",
            deviceModel, androidVersion,
            startTime, endTime, minutes, seconds, stopReason,
            startBattery, endBattery, batteryConsumed,
            hourlyConsumption);
        
        // 儲存到歷史記錄
        TestHistoryActivity.saveTestResult(this, result);
        
        new AlertDialog.Builder(this)
            .setTitle("測試完成")
            .setMessage(result)
            .setPositiveButton("確定", null)
            .setNeutralButton("重新測試", (dialog, which) -> startTest())
            .setNegativeButton("複製結果", (dialog, which) -> {
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("測試結果", result);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "測試結果已複製到剪貼簿", Toast.LENGTH_SHORT).show();
            })
            .show();
    }
    
    private void loadNextUrl() {
        if (currentUrlIndex < testUrls.length && !testUrls[currentUrlIndex].isEmpty()) {
            String url = testUrls[currentUrlIndex];
            webView.loadUrl(url);
            
            String siteName = getSiteName(url);
            tvCurrentUrl.setText(siteName + " " + (currentUrlIndex + 1) + "/5");
            tvStatus.setText("載入 " + siteName + "...");
        }
    }
    
    private String getSiteName(String url) {
        if (url.contains("ahui3c.com")) return "阿輝";
        if (url.contains("pchome.com")) return "PChome";
        if (url.contains("mobile01.com")) return "M01";
        if (url.contains("lpcomment.com")) return "LP";
        if (url.contains("toy-people.com")) return "玩具人";
        return "網站" + (currentUrlIndex + 1);
    }
    
    // 添加網頁時間限制相關變數
    private long currentPageStartTime = 0;
    private static final long MAX_PAGE_TIME = 60 * 1000; // 每個網頁最大1分鐘
    private int scrollAttempts = 0;
    private int lastScrollPosition = 0;
    private static final int MAX_SCROLL_ATTEMPTS = 10; // 最大捲動嘗試次數
    
    private void startScrolling() {
        if (!isTestRunning) return;
        
        // 記錄當前網頁開始時間
        currentPageStartTime = System.currentTimeMillis();
        scrollAttempts = 0;
        lastScrollPosition = 0;
        
        scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isTestRunning) return;
                
                // 檢查網頁時間限制
                long currentTime = System.currentTimeMillis();
                if (currentTime - currentPageStartTime > MAX_PAGE_TIME) {
                    // 超過1分鐘，強制切換到下一個網頁
                    tvStatus.setText("網頁時間到，切換下一個...");
                    switchToNextPage();
                    return;
                }
                
                performSmoothScroll();
                
                // 檢查是否到達底部或卡住
                webView.evaluateJavascript(
                    "(function() { " +
                    "var scrollPos = window.scrollY || window.pageYOffset; " +
                    "var isAtBottom = (window.innerHeight + scrollPos) >= (document.body.offsetHeight - 100); " +
                    "return {scrollPos: scrollPos, isAtBottom: isAtBottom}; " +
                    "})();",
                    result -> {
                        try {
                            // 解析返回的JSON
                            result = result.replace("\"", "");
                            if (result.contains("scrollPos") && result.contains("isAtBottom")) {
                                // 簡單解析
                                boolean isAtBottom = result.contains("isAtBottom:true");
                                String scrollPosStr = result.substring(result.indexOf("scrollPos:") + 10);
                                scrollPosStr = scrollPosStr.substring(0, scrollPosStr.indexOf(","));
                                int currentScrollPos = Integer.parseInt(scrollPosStr.trim());
                                
                                if (isAtBottom) {
                                    // 到達底部，切換到下一個網址
                                    tvStatus.setText("頁面完成，切換下一個...");
                                    switchToNextPage();
                                } else {
                                    // 檢查是否卡住（滾動位置沒有變化）
                                    if (Math.abs(currentScrollPos - lastScrollPosition) < 10) {
                                        scrollAttempts++;
                                        if (scrollAttempts >= MAX_SCROLL_ATTEMPTS) {
                                            // 可能卡住了，強制切換
                                            tvStatus.setText("檢測到卡住，切換下一個...");
                                            switchToNextPage();
                                            return;
                                        }
                                    } else {
                                        scrollAttempts = 0; // 重置嘗試次數
                                    }
                                    
                                    lastScrollPosition = currentScrollPos;
                                    
                                    // 繼續滑動
                                    int interval = getScrollInterval();
                                    handler.postDelayed(this, interval);
                                }
                            } else {
                                // JavaScript執行失敗，使用備用方案
                                scrollAttempts++;
                                if (scrollAttempts >= MAX_SCROLL_ATTEMPTS) {
                                    tvStatus.setText("JavaScript錯誤，切換下一個...");
                                    switchToNextPage();
                                } else {
                                    int interval = getScrollInterval();
                                    handler.postDelayed(this, interval);
                                }
                            }
                        } catch (Exception e) {
                            // 解析錯誤，使用備用方案
                            scrollAttempts++;
                            if (scrollAttempts >= MAX_SCROLL_ATTEMPTS) {
                                tvStatus.setText("解析錯誤，切換下一個...");
                                switchToNextPage();
                            } else {
                                int interval = getScrollInterval();
                                handler.postDelayed(this, interval);
                            }
                        }
                    }
                );
            }
        };
        
        // 初始延遲，讓頁面有時間載入
        handler.postDelayed(scrollRunnable, 3000);
    }
    
    private void switchToNextPage() {
        currentUrlIndex = (currentUrlIndex + 1) % testUrls.length;
        loadNextUrl();
    }
    
    private void performSmoothScroll() {
        // 使用更穩定的捲動方式，避免卡住
        
        // 方法1: 使用JavaScript捲動（更穩定）
        int scrollDistance = 200 + (scrollSpeed * 50); // 根據速度調整捲動距離
        
        webView.evaluateJavascript(
            "(function() { " +
            "window.scrollBy({" +
            "  top: " + scrollDistance + "," +
            "  left: 0," +
            "  behavior: 'smooth'" +
            "}); " +
            "})();", 
            null
        );
        
        // 方法2: 備用觸控捲動（如果JavaScript失敗）
        handler.postDelayed(() -> {
            performTouchScroll();
        }, 100);
    }
    
    private void performTouchScroll() {
        // 備用的觸控捲動方法
        float scrollRatio = 0.15f + (scrollSpeed * 0.02f); // 較小的基礎比例
        
        float startY = webView.getHeight() * 0.6f; // 從螢幕60%位置開始
        float endY = webView.getHeight() * (0.6f - scrollRatio); // 根據速度調整結束位置
        
        // 確保滑動距離合理
        float minEndY = webView.getHeight() * 0.2f;
        float maxEndY = webView.getHeight() * 0.5f;
        endY = Math.max(minEndY, Math.min(maxEndY, endY));
        
        // 使用較少的步數，避免過度複雜
        final int totalSteps = 12; // 減少步數
        final int stepDelay = 20; // 稍微增加間隔
        
        for (int i = 0; i < totalSteps; i++) {
            final int step = i;
            final float finalEndY = endY;
            handler.postDelayed(() -> {
                if (!isTestRunning) return; // 確保測試還在進行
                
                // 使用線性插值，更簡單穩定
                float progress = (float) step / (totalSteps - 1);
                float currentY = startY + (finalEndY - startY) * progress;
                
                long downTime = System.currentTimeMillis();
                int action = (step == 0) ? MotionEvent.ACTION_DOWN : 
                           (step == totalSteps - 1) ? MotionEvent.ACTION_UP : MotionEvent.ACTION_MOVE;
                
                MotionEvent event = MotionEvent.obtain(downTime, downTime, 
                    action, webView.getWidth() / 2f, currentY, 0);
                
                webView.dispatchTouchEvent(event);
                event.recycle();
            }, step * stepDelay);
        }
    }
    
    private int getScrollInterval() {
        // 使用用戶設定的停留時間（秒轉毫秒）
        return (int)(stayTimeSeconds * 1000);
    }
    
    private void startTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isTestRunning) {
                    long elapsed = System.currentTimeMillis() - testStartTime;
                    long totalTestTime = testDurationMinutes * 60 * 1000; // 總測試時間（毫秒）
                    long remaining = totalTestTime - elapsed;
                    
                    if (remaining <= 0) {
                        // 時間到了，自動停止測試
                        remaining = 0;
                        if (isTestRunning) {
                            stopTestWithResults();
                        }
                        return;
                    }
                    
                    long minutes = remaining / (1000 * 60);
                    long seconds = (remaining % (1000 * 60)) / 1000;
                    
                    tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }
    
    private int getCurrentBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            return (int) ((level / (float) scale) * 100);
        }
        return 0;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        updateBatteryLevel();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
        if (toneGenerator != null) {
            toneGenerator.release();
        }
        handler.removeCallbacksAndMessages(null);
        testHandler.removeCallbacksAndMessages(null);
    }
}

