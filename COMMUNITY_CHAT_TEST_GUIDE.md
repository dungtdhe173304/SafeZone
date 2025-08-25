# 🧪 Hướng Dẫn Test Hệ Thống Chat Cộng Đồng

## ✅ **Trạng thái hiện tại:**
- ✅ Build thành công
- ✅ Tất cả files đã được tạo
- ✅ Layout đã được tích hợp
- ✅ Database integration hoàn tất

## 🚀 **Cách Test:**

### **1. Chạy App và Kiểm Tra Header:**
1. **Mở app SafeZone**
2. **Đăng nhập** với tài khoản có sẵn
3. **Kiểm tra header** - bạn sẽ thấy:
   - 💬 Chat Cộng Đồng
   - Nút "Xem tất cả"
   - Tin nhắn mặc định: "Chào mừng đến với SafeZone! 💬"
   - **Tin nhắn sẽ chạy từ phải qua trái** (animation)

### **2. Test Gửi Tin Nhắn:**
1. **Mở menu drawer** (vuốt từ trái sang phải)
2. **Chọn "Chat Cộng Đồng"**
3. **Kiểm tra giao diện:**
   - Số dư hiện tại
   - Chi phí: 5,000 VND mỗi tin nhắn
   - Input field (tối đa 200 ký tự)
   - Nút "Gửi"

### **3. Test Logic Trừ Tiền:**
1. **Nhập tin nhắn** (ví dụ: "Hello SafeZone!")
2. **Nhấn "Gửi"**
3. **Kiểm tra:**
   - ✅ Tin nhắn được gửi
   - ✅ Số dư bị trừ 5,000 VND
   - ✅ Tin nhắn xuất hiện ở header
   - ✅ Tin nhắn chạy từ phải qua trái

### **4. Test Không Đủ Tiền:**
1. **Gửi tin nhắn** cho đến khi hết tiền
2. **Kiểm tra dialog:**
   - "Số dư không đủ"
   - "Bạn cần 5,000 VND để gửi tin nhắn"
   - Nút "Nạp tiền" → Chuyển đến Wallet
   - Nút "Hủy"

## 🔍 **Debug và Troubleshooting:**

### **Nếu Header Không Hiển Thị:**
1. **Kiểm tra Logcat:**
   ```
   Filter: "CommunityChatHeaderView"
   ```
2. **Tìm log:**
   ```
   "setMessages called with X messages"
   ```

### **Nếu Animation Không Chạy:**
1. **Kiểm tra view width:**
   ```java
   Log.d("CommunityChatHeaderView", "View width: " + getWidth());
   ```
2. **Đảm bảo view đã được layout**

### **Nếu Tin Nhắn Không Được Gửi:**
1. **Kiểm tra balance:**
   ```java
   Log.d("CommunityChatService", "User balance: " + user.getBalance());
   ```
2. **Kiểm tra database:**
   ```java
   Log.d("CommunityChatService", "Message inserted: " + chatMessage.getId());
   ```

## 📱 **Các Màn Hình Cần Kiểm Tra:**

### **MainActivity:**
- ✅ Header chat hiển thị
- ✅ Tin nhắn chạy từ phải qua trái
- ✅ Nút "Xem tất cả" hoạt động

### **CommunityChatActivity:**
- ✅ Toolbar với title "Chat Cộng Đồng"
- ✅ Hiển thị số dư và chi phí
- ✅ RecyclerView hiển thị tin nhắn
- ✅ Input field và nút gửi
- ✅ Dialog khi không đủ tiền

### **Drawer Menu:**
- ✅ Menu item "Chat Cộng Đồng"
- ✅ Icon và text hiển thị đúng

## 🎯 **Kết Quả Mong Đợi:**

### **Sau khi test thành công:**
1. **Header hiển thị tin nhắn chạy từ phải qua trái**
2. **Gửi tin nhắn thành công và trừ tiền**
3. **Tin nhắn mới xuất hiện ở header ngay lập tức**
4. **Navigation hoạt động mượt mà**
5. **Balance được cập nhật real-time**

### **Nếu có vấn đề:**
1. **Kiểm tra Logcat** để xem lỗi
2. **Kiểm tra database** có tin nhắn không
3. **Kiểm tra balance** của user
4. **Kiểm tra layout** có hiển thị đúng không

## 🚨 **Lưu Ý Quan Trọng:**

1. **Đảm bảo user đã đăng nhập** trước khi test
2. **Kiểm tra balance** trước khi gửi tin nhắn
3. **Test trên device thật** để animation mượt mà
4. **Kiểm tra log** nếu có vấn đề

## 📞 **Hỗ Trợ:**

Nếu gặp vấn đề, hãy:
1. **Chụp màn hình** lỗi
2. **Copy log** từ Logcat
3. **Mô tả** bước thực hiện
4. **Gửi thông tin** để được hỗ trợ

---

**🎉 Chúc bạn test thành công! Hệ thống chat cộng đồng đã sẵn sàng hoạt động!**
