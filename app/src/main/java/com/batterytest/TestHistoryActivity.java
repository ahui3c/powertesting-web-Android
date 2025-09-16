package com.batterytest;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestHistoryActivity extends AppCompatActivity {
    
    private LinearLayout historyContainer;
    private Button btnClearHistory;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_history);
        
        initViews();
        loadTestHistory();
    }
    
    private void initViews() {
        historyContainer = findViewById(R.id.historyContainer);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        sharedPreferences = getSharedPreferences("BatteryTestPrefs", MODE_PRIVATE);
        
        btnClearHistory.setOnClickListener(v -> showClearHistoryDialog());
    }
    
    private void loadTestHistory() {
        historyContainer.removeAllViews();
        
        // 獲取歷史記錄數量
        int historyCount = sharedPreferences.getInt("history_count", 0);
        
        if (historyCount == 0) {
            TextView noHistoryText = new TextView(this);
            noHistoryText.setText("尚無測試歷史記錄");
            noHistoryText.setTextSize(16);
            noHistoryText.setTextColor(getColor(android.R.color.darker_gray));
            noHistoryText.setPadding(32, 32, 32, 32);
            historyContainer.addView(noHistoryText);
            btnClearHistory.setVisibility(View.GONE);
            return;
        }
        
        btnClearHistory.setVisibility(View.VISIBLE);
        
        // 載入歷史記錄（從最新到最舊）
        for (int i = historyCount - 1; i >= 0; i--) {
            String historyData = sharedPreferences.getString("history_" + i, "");
            if (!historyData.isEmpty()) {
                addHistoryItem(historyData, i);
            }
        }
    }
    
    private void addHistoryItem(String historyData, int index) {
        View historyItem = getLayoutInflater().inflate(R.layout.item_test_history, null);
        
        TextView tvHistoryContent = historyItem.findViewById(R.id.tvHistoryContent);
        Button btnCopyHistory = historyItem.findViewById(R.id.btnCopyHistory);
        Button btnDeleteHistory = historyItem.findViewById(R.id.btnDeleteHistory);
        
        tvHistoryContent.setText(historyData);
        
        btnCopyHistory.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("測試結果", historyData);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "測試結果已複製到剪貼簿", Toast.LENGTH_SHORT).show();
        });
        
        btnDeleteHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("刪除記錄")
                .setMessage("確定要刪除這筆測試記錄嗎？")
                .setPositiveButton("刪除", (dialog, which) -> {
                    deleteHistoryItem(index);
                    loadTestHistory();
                })
                .setNegativeButton("取消", null)
                .show();
        });
        
        historyContainer.addView(historyItem);
    }
    
    private void deleteHistoryItem(int index) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int historyCount = sharedPreferences.getInt("history_count", 0);
        
        // 移除指定的記錄
        editor.remove("history_" + index);
        
        // 重新排列剩餘的記錄
        for (int i = index + 1; i < historyCount; i++) {
            String data = sharedPreferences.getString("history_" + i, "");
            if (!data.isEmpty()) {
                editor.putString("history_" + (i - 1), data);
            }
            editor.remove("history_" + i);
        }
        
        // 更新記錄數量
        editor.putInt("history_count", historyCount - 1);
        editor.apply();
    }
    
    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
            .setTitle("清除歷史記錄")
            .setMessage("確定要清除所有測試歷史記錄嗎？此操作無法復原。")
            .setPositiveButton("清除", (dialog, which) -> {
                clearAllHistory();
                loadTestHistory();
                Toast.makeText(this, "歷史記錄已清除", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void clearAllHistory() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int historyCount = sharedPreferences.getInt("history_count", 0);
        
        // 刪除所有歷史記錄
        for (int i = 0; i < historyCount; i++) {
            editor.remove("history_" + i);
        }
        
        editor.putInt("history_count", 0);
        editor.apply();
    }
    
    public static void saveTestResult(Context context, String testResult) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("BatteryTestPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        int historyCount = sharedPreferences.getInt("history_count", 0);
        
        // 添加時間戳記
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String historyEntry = "記錄時間: " + timestamp + "\n\n" + testResult;
        
        // 儲存新記錄
        editor.putString("history_" + historyCount, historyEntry);
        editor.putInt("history_count", historyCount + 1);
        
        // 限制歷史記錄數量（最多保留50筆）
        if (historyCount >= 50) {
            // 刪除最舊的記錄
            editor.remove("history_0");
            // 重新排列記錄
            for (int i = 1; i <= historyCount; i++) {
                String data = sharedPreferences.getString("history_" + i, "");
                if (!data.isEmpty()) {
                    editor.putString("history_" + (i - 1), data);
                }
                editor.remove("history_" + i);
            }
        } else {
            editor.putInt("history_count", historyCount + 1);
        }
        
        editor.apply();
    }
}

