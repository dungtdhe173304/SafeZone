# Hệ thống Đăng ký Tham gia Đấu giá - SafeZone

## Tổng quan

Hệ thống này cho phép người dùng đăng ký tham gia các phiên đấu giá với quy trình xác thực và duyệt của admin. Khi đăng ký thành công, người dùng sẽ được trừ tiền cọc (startPrice) và có thể tham gia vào phòng đấu giá.

## Các thành phần chính

### 1. AuctionRegistrationService
- **Vị trí**: `app/src/main/java/com/group5/safezone/service/AuctionRegistrationService.java`
- **Chức năng**: Xử lý logic đăng ký, duyệt, và kiểm tra điều kiện tham gia đấu giá

#### Các method chính:
- `registerForAuction()`: Đăng ký tham gia đấu giá
- `approveRegistration()`: Admin duyệt/từ chối đăng ký
- `checkEligibility()`: Kiểm tra điều kiện tham gia
- `cancelRegistration()`: Hủy đăng ký

### 2. AuctionRegistrationActivity
- **Vị trí**: `app/src/main/java/com/group5/safezone/view/AuctionRegistrationActivity.java`
- **Layout**: `app/src/main/res/layout/activity_auction_registration.xml`
- **Chức năng**: Giao diện cho người dùng đăng ký tham gia đấu giá

#### Tính năng:
- Hiển thị thông tin sản phẩm và phiên đấu giá
- Hiển thị số dư hiện tại và tiền cọc cần thiết
- Hiển thị trạng thái đăng ký
- Nút đăng ký và vào phòng đấu giá

### 3. AdminAuctionApprovalActivity
- **Vị trí**: `app/src/main/java/com/group5/safezone/view/AdminAuctionApprovalActivity.java`
- **Layout**: `app/src/main/res/layout/activity_admin_auction_approval.xml`
- **Chức năng**: Giao diện cho admin duyệt các đăng ký

#### Tính năng:
- Danh sách các đăng ký chờ duyệt
- Nút duyệt/từ chối từng đăng ký
- Hiển thị thông tin chi tiết đăng ký

### 4. AuctionRoomActivity
- **Vị trí**: `app/src/main/java/com/group5/safezone/view/AuctionRoomActivity.java`
- **Layout**: `app/src/main/res/layout/activity_auction_room.xml`
- **Chức năng**: Phòng đấu giá (đang phát triển)

## Quy trình hoạt động

### 1. Đăng ký tham gia đấu giá
1. Người dùng chọn phiên đấu giá muốn tham gia
2. Hệ thống kiểm tra:
   - Phiên đấu giá còn hoạt động không
   - Thời gian đấu giá hợp lệ
   - Người dùng chưa đăng ký
3. Tạo đăng ký với trạng thái "pending"
4. Lưu vào bảng `auctionRegistrations`

### 2. Admin duyệt đăng ký
1. Admin xem danh sách đăng ký chờ duyệt
2. Kiểm tra số dư người dùng
3. Nếu duyệt:
   - Trừ tiền cọc từ balance của user
   - Cập nhật trạng thái thành "approved"
4. Nếu từ chối:
   - Cập nhật trạng thái thành "rejected"

### 3. Tham gia đấu giá
1. Người dùng có trạng thái "approved" có thể vào phòng đấu giá
2. Hệ thống kiểm tra điều kiện trước khi cho phép vào

## Cấu trúc Database

### Bảng auctionRegistrations
- `id`: ID đăng ký
- `auctionId`: ID phiên đấu giá
- `userId`: ID người dùng
- `paymentAmount`: Số tiền cọc (startPrice)
- `status`: Trạng thái (pending/approved/rejected/cancelled)
- `paymentDate`: Ngày đăng ký
- `createdAt`, `updatedAt`: Thời gian tạo/cập nhật

### Bảng user
- `balance`: Số dư tài khoản (sẽ được trừ tiền cọc khi duyệt)

## Cách sử dụng

### 1. Đăng ký tham gia đấu giá
```java
// Trong Activity
Intent intent = new Intent(this, AuctionRegistrationActivity.class);
intent.putExtra(AuctionRegistrationActivity.EXTRA_AUCTION_ID, auctionId);
intent.putExtra(AuctionRegistrationActivity.EXTRA_AUCTION_ITEM, auctionItem);
startActivity(intent);
```

### 2. Admin duyệt đăng ký
```java
// Trong AdminActivity
Intent intent = new Intent(this, AdminAuctionApprovalActivity.class);
startActivity(intent);
```

### 3. Vào phòng đấu giá
```java
// Sau khi được duyệt
Intent intent = new Intent(this, AuctionRoomActivity.class);
intent.putExtra(AuctionRoomActivity.EXTRA_AUCTION_ID, auctionId);
startActivity(intent);
```

## Lưu ý quan trọng

1. **Quyền truy cập**: Chỉ admin mới có thể duyệt đăng ký
2. **Kiểm tra số dư**: Hệ thống sẽ kiểm tra số dư trước khi duyệt
3. **Trạng thái đăng ký**: 
   - `pending`: Chờ duyệt
   - `approved`: Đã duyệt, có thể tham gia
   - `rejected`: Bị từ chối
   - `cancelled`: Đã hủy
4. **Tiền cọc**: Sẽ được trừ từ balance khi admin duyệt
5. **Hoàn tiền**: Nếu hủy đăng ký sau khi được duyệt, tiền cọc sẽ được hoàn lại

## Phát triển tiếp theo

1. **Phòng đấu giá**: Implement real-time bidding, chat, timer
2. **Thông báo**: Push notification khi đăng ký được duyệt/từ chối
3. **Lịch sử**: Xem lịch sử đăng ký và tham gia đấu giá
4. **Báo cáo**: Thống kê số lượng đăng ký, tỷ lệ duyệt
5. **Tích hợp**: Kết nối với hệ thống thanh toán thực tế

## Troubleshooting

### Lỗi thường gặp:
1. **"Không đủ số dư"**: Kiểm tra balance của user
2. **"Đã đăng ký"**: Kiểm tra trạng thái đăng ký hiện tại
3. **"Phiên đấu giá không hợp lệ"**: Kiểm tra thời gian và trạng thái auction

### Debug:
- Sử dụng Log.d với tag "AuctionRegistrationService"
- Kiểm tra database trực tiếp
- Xem log trong Android Studio
