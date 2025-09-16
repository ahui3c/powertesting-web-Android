# 測電力_網頁 (PowerTesting Web) v0.5

<div align="center">
  <img src="images/app_icon.png" alt="應用程式圖示" width="128" height="128">
  <br>
  <strong>一個專為Android設計的網頁瀏覽電力測試應用程式</strong>
  <br>
  可以自動模擬用戶瀏覽網頁的行為，並精確記錄電池消耗情況
</div>

## 📱 應用程式截圖

<div align="center">
  <table>
    <tr>
      <td align="center">
        <img src="images/screenshots/settings_overview.png" alt="設定頁面總覽" width="250">
        <br>
        <strong>設定頁面總覽</strong>
      </td>
      <td align="center">
        <img src="images/screenshots/settings_page.png" alt="網址設定頁面" width="250">
        <br>
        <strong>網址設定頁面</strong>
      </td>
      <td align="center">
        <img src="images/screenshots/test_result.png" alt="測試結果報告" width="250">
        <br>
        <strong>測試結果報告</strong>
      </td>
    </tr>
  </table>
</div>

## 📱 功能特色

### 🔋 電力測試
- **自動化測試**: 全自動模擬人工瀏覽網頁行為
- **精確記錄**: 記錄測試開始和結束的電量變化
- **詳細報告**: 提供完整的測試結果和電量消耗分析
- **可調時間**: 支援1-120分鐘的測試時間設定

### 🌐 網頁瀏覽
- **內嵌瀏覽器**: 使用WebView提供完整的網頁瀏覽體驗
- **多網址循環**: 支援5組網址循環測試
- **智能切換**: 滑動到頁面底部時自動切換到下一個網址
- **預設網站**: 內建台灣本土網站作為預設測試對象

### ⚡ 滑動模擬
- **真實滑動**: 模擬真實手指滑動，非按鍵操作
- **可調速度**: 10級滑動速度可調整
- **平滑動畫**: 分步驟產生平滑的滑動動畫
- **底部檢測**: 智能檢測頁面底部並觸發切換

### 🎨 使用者界面
- **精簡設計**: 最大化瀏覽器顯示空間
- **即時狀態**: 顯示當前電量、網址和測試時間
- **防休眠**: 測試期間保持螢幕常亮
- **120Hz支援**: 支援高更新率顯示器

## 📊 測試結果

應用程式會產生詳細的測試報告，包含：

```
測電力_網頁 測試結果報告

裝置資訊:
手機型號: Samsung Galaxy Z Fold5 (Android 14)

測試時間:
開始: 14:30:15
結束: 15:00:28
總時長: 30分13秒

電量變化:
開始電量: 85%
結束電量: 78%
消耗電量: 7%

每小時消耗: 13.9%
```

## 🛠️ 技術規格

- **最低系統**: Android 7.0 (API 24)
- **目標系統**: Android 14 (API 34)
- **開發語言**: Java
- **UI框架**: Android原生 + Material Design
- **瀏覽器**: WebView with JavaScript支援
- **特殊功能**: 120Hz高更新率支援

## 📥 下載安裝

### APK下載
- [測電力_網頁-v0.1.apk](releases/測電力_網頁-v0.1.apk) - 最新版本

### 安裝步驟
1. 下載APK檔案到Android裝置
2. 開啟「未知來源」安裝權限
3. 點擊APK檔案進行安裝
4. 安裝完成後即可使用

## ⚙️ 使用說明

### 基本操作
1. **開啟應用程式**: 點擊桌面圖示啟動
2. **進入設定**: 點擊右下角「設定」按鈕
3. **調整參數**: 設定測試時間、滑動速度和網址
4. **開始測試**: 返回主頁面點擊「執行」開始測試
5. **查看結果**: 測試完成後查看詳細報告

### 設定選項
- **測試時間**: 1-120分鐘可調
- **滑動速度**: 1-10級速度設定
- **網址設定**: 5組網址循環配置
- **預設網址**: 
  - https://ahui3c.com
  - https://www.pchome.com.tw
  - https://m.mobile01.com
  - https://lpcomment.com/
  - https://www.toy-people.com/

## 🔧 開發環境

### 建置需求
- Android Studio Arctic Fox 或更新版本
- Android SDK 34
- Java 8 或更新版本
- Gradle 8.0

### 專案結構
```
app/
├── src/main/
│   ├── java/com/batterytest/
│   │   ├── MainActivity.java
│   │   └── SettingsActivity.java
│   ├── res/
│   │   ├── layout/
│   │   ├── values/
│   │   └── mipmap-*/
│   └── AndroidManifest.xml
├── build.gradle
└── proguard-rules.pro
```

### 建置指令
```bash
# Debug版本
./gradlew assembleDebug

# Release版本
./gradlew assembleRelease
```

## 📝 版本歷史

### v0.1 (2024-09-08)
- 🎉 首次發布
- ✅ 基本電力測試功能
- ✅ 多網址循環瀏覽
- ✅ 自動滑動模擬
- ✅ 詳細測試報告
- ✅ 設定頁面
- ✅ 120Hz高更新率支援
- ✅ 精美應用程式圖示

## 🤝 貢獻指南

歡迎提交Issue和Pull Request來改進這個專案！

### 提交Issue
- 詳細描述問題或建議
- 提供裝置型號和Android版本
- 如果是Bug，請提供重現步驟

### 提交Pull Request
- Fork這個專案
- 建立feature分支
- 提交你的修改
- 發起Pull Request

## 📄 授權條款

本專案採用 [MIT License](LICENSE) 開源授權。

## 👨‍💻 作者

**廖阿輝**
- 網站: [https://ahui3c.com](https://ahui3c.com)
- 專長: Android開發、3C評測、科技寫作

## 🙏 致謝

感謝所有測試用戶的回饋和建議，讓這個應用程式能夠不斷改進。

## 📞 支援

如果您在使用過程中遇到問題，請：
1. 查看本README的使用說明
2. 搜尋已有的Issues
3. 提交新的Issue描述問題
4. 訪問作者網站獲取更多資訊

---

**注意**: 本應用程式僅供測試和研究用途，請合理使用並注意電池健康。

