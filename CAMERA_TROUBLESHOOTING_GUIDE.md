# 🔍 Hướng Dẫn Khắc Phục Camera & Microphone

## 🚨 **Vấn Đề: Camera Không Hiển Thị Trong Live Stream**

### **🔍 Nguyên Nhân Chính:**

1. **Quyền Camera chưa được cấp**
2. **Camera bị tắt trong ZEGOCLOUD UI**
3. **Cấu hình camera không đúng**
4. **Camera đang được sử dụng bởi app khác**
5. **Vấn đề phần cứng hoặc phần mềm**

---

## ✅ **Giải Pháp Từng Bước:**

### **Bước 1: Kiểm Tra Quyền**
1. Mở **Settings** > **Apps** > **SafeZone**
2. Chọn **Permissions**
3. Đảm bảo **Camera** và **Microphone** được **Allow**
4. Nếu bị **Deny**, hãy **Allow** lại

### **Bước 2: Kiểm Tra Trong App**
1. Mở **SafeZone** app
2. Vào **Live Streaming**
3. Nhấn nút **"📷 Check"** để kiểm tra camera
4. Nhấn nút **"🎤 Check"** để kiểm tra microphone
5. Xem thông báo trạng thái

### **Bước 2.5: Sử Dụng ZEGOCLOUD UI**
1. **Bắt đầu live stream** (Start Live Stream)
2. **Trong giao diện ZEGOCLOUD, tìm:**
   - 📷 **Camera Button** - Nhấn để bật/tắt camera
   - 🎤 **Microphone Button** - Nhấn để bật/tắt mic
   - 🔄 **Switch Camera** - Chuyển đổi front/back camera
   - ⚙️ **Settings** - Cài đặt video/audio quality
3. **Camera sẽ hiển thị** khi bạn nhấn nút camera

### **Bước 3: Khởi Động Lại Live Stream**
1. **Stop** live stream hiện tại
2. **Close** app SafeZone
3. **Clear** app cache (Settings > Apps > SafeZone > Storage > Clear Cache)
4. **Restart** app và thử lại

---

## 🔧 **Khắc Phục Nâng Cao:**

### **Vấn Đề 1: Camera Bị Tắt**
```
Trong LiveStreamingActivity:
- ZEGOCLOUD UI Kit tự động xử lý camera và microphone ✅
- Host sẽ thấy nút điều khiển camera/mic trong giao diện ✅
- Không cần cấu hình thủ công các thuộc tính camera ✅
```

### **Vấn Đề 2: Quyền Bị Từ Chối**
```
AndroidManifest.xml:
- android.permission.CAMERA ✅
- android.permission.RECORD_AUDIO ✅
- android.permission.MODIFY_AUDIO_SETTINGS ✅
```

### **Vấn Đề 3: Cấu Hình ZEGOCLOUD**
```
ZEGOCLOUD Config:
- APP_ID: 306600199 ✅
- APP_SIGN: 320f9747bd7cfc0c891f592df9166e7bc611968b8ed9fbc9ff43909f216036fa ✅
- UI Kit tự động xử lý camera/microphone ✅
- Host có nút điều khiển camera/mic trong giao diện ✅
```

---

## 📱 **Kiểm Tra Thiết Bị:**

### **Test Camera:**
1. Mở **Camera app** mặc định
2. Chụp ảnh và quay video
3. Chuyển đổi giữa **Front/Back** camera
4. Kiểm tra xem có hoạt động bình thường không

### **Test Microphone:**
1. Mở **Voice Recorder** app
2. Ghi âm và phát lại
3. Kiểm tra xem có nghe được âm thanh không
4. Kiểm tra **volume** và **mute** settings

---

## 🎯 **Cách ZEGOCLOUD Hoạt Động:**

### **ZEGOCLOUD UI Kit Tự Động:**
1. **Không cần cấu hình thủ công** camera/microphone
2. **Giao diện có sẵn nút điều khiển** camera/mic
3. **Host sẽ thấy:**
   - 📷 Nút bật/tắt camera
   - 🎤 Nút bật/tắt microphone
   - 🔄 Nút chuyển đổi camera (front/back)
   - ⚙️ Nút cài đặt video/audio

### **Để Bật Camera:**
1. **Bắt đầu live stream** (Start Live Stream)
2. **Nhìn vào giao diện ZEGOCLOUD**
3. **Tìm nút camera** (thường có icon 📷)
4. **Nhấn để bật camera**
5. **Camera sẽ hiển thị** trong stream

---

## 🚀 **Giải Pháp Nhanh:**

### **Nếu Camera Vẫn Không Hoạt Động:**

1. **Restart Device**
   - Tắt và bật lại thiết bị
   - Đây là giải pháp hiệu quả nhất

2. **Clear All App Data**
   - Settings > Apps > SafeZone > Storage
   - Clear Data + Clear Cache
   - Restart app

3. **Check Other Apps**
   - Đóng tất cả app khác
   - Đặc biệt là Camera, Instagram, Snapchat
   - Chỉ để SafeZone chạy

4. **Update Software**
   - Kiểm tra Android version
   - Update lên phiên bản mới nhất

---

## 🔍 **Debug Information:**

### **Logs để Kiểm Tra:**
```
adb logcat | grep -i "camera\|zego\|livestream"
```

### **Kiểm Tra Permissions:**
```
adb shell dumpsys package com.group5.safezone | grep permission
```

---

## 📞 **Liên Hệ Hỗ Trợ:**

### **Nếu Vấn Đề Vẫn Tiếp Tục:**

1. **Ghi lại thông tin:**
   - Android version
   - Device model
   - Error messages
   - Steps to reproduce

2. **Chụp màn hình:**
   - Permission settings
   - Error dialogs
   - Live streaming screen

3. **Gửi thông tin cho developer:**
   - Email: [your-email]
   - Issue tracker: [link]

---

## 🎯 **Kết Quả Mong Đợi:**

Sau khi làm theo hướng dẫn này, bạn sẽ thấy:

- ✅ **Camera hoạt động bình thường**
- ✅ **Video stream hiển thị rõ ràng**
- ✅ **Microphone ghi âm tốt**
- ✅ **Live stream ổn định**

---

## 💡 **Lời Khuyên:**

1. **Luôn kiểm tra quyền trước khi stream**
2. **Đóng các app khác khi live stream**
3. **Sử dụng kết nối internet ổn định**
4. **Kiểm tra pin và nhiệt độ thiết bị**
5. **Test camera/mic trước khi stream chính**

---

**🎉 Chúc bạn live stream thành công! Nếu vẫn gặp vấn đề, hãy liên hệ hỗ trợ!**
