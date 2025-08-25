# Hệ Thống Chat Cộng Đồng SafeZone

## Tổng Quan
Hệ thống chat cộng đồng cho phép tất cả người dùng trong SafeZone có thể nhắn tin với nhau. Mỗi tin nhắn có chi phí 5,000 VND và được hiển thị real-time ở header của tất cả các giao diện chính.

## Tính Năng Chính

### 1. Gửi Tin Nhắn
- **Giới hạn**: Tối đa 200 ký tự mỗi tin nhắn
- **Chi phí**: 5,000 VND mỗi tin nhắn
- **Kiểm tra balance**: Tự động kiểm tra số dư trước khi cho phép gửi
- **Gợi ý nạp tiền**: Nếu không đủ tiền, sẽ hiển thị dialog gợi ý nạp tiền

### 2. Hiển Thị Real-Time
- **Header chạy**: Tin nhắn chạy từ phải qua trái ở header
- **Animation**: Sử dụng Android Animation API để tạo hiệu ứng mượt mà
- **Queue system**: Tin nhắn được xử lý theo thứ tự queue
- **Thời gian hiển thị**: Mỗi tin nhắn hiển thị 3 giây trước khi chuyển sang tin nhắn tiếp theo

### 3. Giao Diện
- **Header component**: Hiển thị ở tất cả các màn hình chính
- **Community Chat Activity**: Trang riêng để xem và gửi tin nhắn
- **Message list**: Hiển thị 50 tin nhắn gần nhất
- **Balance display**: Hiển thị số dư hiện tại và chi phí tin nhắn

## Cấu Trúc Code

### 1. Service Layer
- **CommunityChatService**: Quản lý logic gửi tin nhắn, kiểm tra balance, và xử lý queue
- **MessageDisplayListener**: Interface để lắng nghe sự kiện tin nhắn

### 2. View Layer
- **CommunityChatHeaderView**: Component hiển thị ở header với animation
- **CommunityChatActivity**: Activity chính để gửi và xem tin nhắn
- **CommunityChatAdapter**: Adapter cho RecyclerView hiển thị danh sách tin nhắn

### 3. Model Layer
- **ChatCommunity**: Entity chứa thông tin tin nhắn
- **ChatCommunityDao**: Data Access Object để truy cập database

### 4. Layout Files
- `activity_community_chat.xml`: Layout cho CommunityChatActivity
- `item_community_message.xml`: Layout cho item tin nhắn
- `component_community_chat_header.xml`: Layout cho header component

## Cách Sử Dụng

### 1. Gửi Tin Nhắn
1. Mở menu drawer và chọn "Chat Cộng Đồng"
2. Kiểm tra số dư hiện tại
3. Nhập tin nhắn (tối đa 200 ký tự)
4. Nhấn "Gửi"
5. Nếu không đủ tiền, sẽ hiển thị dialog gợi ý nạp tiền

### 2. Xem Tin Nhắn
- **Header**: Tin nhắn tự động chạy từ phải qua trái
- **Activity**: Xem danh sách 50 tin nhắn gần nhất
- **Real-time**: Tin nhắn mới sẽ xuất hiện ngay lập tức

### 3. Navigation
- **Menu item**: "Chat Cộng Đồng" trong drawer menu
- **Header button**: "Xem tất cả" để mở CommunityChatActivity
- **Back navigation**: Sử dụng nút back hoặc up button

## Tích Hợp

### 1. MainActivity
- Tự động khởi tạo CommunityChatService
- Hiển thị CommunityChatHeaderView ở header
- Load tin nhắn gần nhất khi khởi động

### 2. Database
- Sử dụng Room database hiện có
- Không thay đổi schema database
- Tích hợp với User entity để kiểm tra balance

### 3. Wallet Integration
- Tự động chuyển đến WalletActivity khi cần nạp tiền
- Cập nhật balance real-time sau khi gửi tin nhắn

## Bảo Mật

### 1. Authentication
- Kiểm tra user đăng nhập trước khi cho phép gửi tin nhắn
- Sử dụng SessionManager để xác thực

### 2. Balance Validation
- Kiểm tra balance trước mỗi lần gửi tin nhắn
- Trừ tiền tự động sau khi gửi thành công
- Rollback nếu có lỗi xảy ra

### 3. Input Validation
- Kiểm tra độ dài tin nhắn (tối đa 200 ký tự)
- Sanitize input để tránh XSS
- Kiểm tra tin nhắn rỗng

## Performance

### 1. Queue System
- Sử dụng LinkedBlockingQueue để quản lý tin nhắn
- Background thread để xử lý tin nhắn
- Không block UI thread

### 2. Animation
- Sử dụng Android Animation API
- Hardware acceleration cho smooth animation
- Pause/resume animation khi cần thiết

### 3. Database
- Async operations cho database
- Chỉ load tin nhắn cần thiết (50 tin nhắn gần nhất)
- Efficient queries với Room

## Troubleshooting

### 1. Tin nhắn không hiển thị
- Kiểm tra CommunityChatService đã được khởi tạo
- Kiểm tra MessageDisplayListener đã được set
- Kiểm tra database connection

### 2. Animation không chạy
- Kiểm tra view width đã được set
- Kiểm tra animation không bị cancel
- Kiểm tra isAnimating flag

### 3. Balance không cập nhật
- Kiểm tra database transaction
- Kiểm tra User entity update
- Kiểm tra SessionManager

## Tương Lai

### 1. Tính năng có thể thêm
- Emoji support
- File/image sharing
- User blocking
- Message moderation
- Push notifications

### 2. Cải thiện performance
- WebSocket thay vì polling
- Message caching
- Lazy loading
- Background sync

### 3. UI/UX
- Dark mode support
- Custom themes
- Accessibility improvements
- Multi-language support
