package com.group5.safezone.config;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.group5.safezone.model.dao.UserDao;
import com.group5.safezone.model.dao.NotificationDao;
import com.group5.safezone.model.dao.VerifyDao;
import com.group5.safezone.model.dao.ChatCommunityDao;
import com.group5.safezone.model.dao.ProductDao;
import com.group5.safezone.model.dao.OrderDao;
import com.group5.safezone.model.dao.ProductImagesDao;
import com.group5.safezone.model.dao.ReportDao;
import com.group5.safezone.model.dao.CommentDao;
import com.group5.safezone.model.dao.AuctionsDao;
import com.group5.safezone.model.dao.AuctionRegistrationsDao;
import com.group5.safezone.model.dao.BidsDao;
import com.group5.safezone.model.dao.TransactionsDao;

import com.group5.safezone.model.entity.User;
import com.group5.safezone.model.entity.Notification;
import com.group5.safezone.model.entity.Verify;
import com.group5.safezone.model.entity.ChatCommunity;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.Order;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.model.entity.Report;
import com.group5.safezone.model.entity.Comment;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.AuctionRegistrations;
import com.group5.safezone.model.entity.Bids;
import com.group5.safezone.model.entity.Transactions;

@Database(entities = {
        User.class,
        Notification.class,
        Verify.class,
        ChatCommunity.class,
        Product.class,
        Order.class,
        ProductImages.class,
        Report.class,
        Comment.class,
        Auctions.class,
        AuctionRegistrations.class,
        Bids.class,
        Transactions.class
}, version = 4, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    // Abstract methods cho các DAO
    public abstract UserDao userDao();
    public abstract NotificationDao notificationDao();
    public abstract VerifyDao verifyDao();
    public abstract ChatCommunityDao chatCommunityDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract ProductImagesDao productImagesDao();
    public abstract ReportDao reportDao();
    public abstract CommentDao commentDao();
    public abstract AuctionsDao auctionsDao();
    public abstract AuctionRegistrationsDao auctionRegistrationsDao();
    public abstract BidsDao bidsDao();
    public abstract TransactionsDao transactionsDao();

    private static volatile AppDatabase INSTANCE;

    // Migration từ version 1 lên 2
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Thêm các cột mới vào bảng Transactions
            database.execSQL("ALTER TABLE Transactions ADD COLUMN referenceId TEXT");
            database.execSQL("ALTER TABLE Transactions ADD COLUMN createdAt INTEGER");
            database.execSQL("ALTER TABLE Transactions ADD COLUMN updatedAt INTEGER");
            
            // Rename cột UserId thành userId để match với entity
            try {
                database.execSQL("ALTER TABLE Transactions RENAME COLUMN UserId TO userId");
            } catch (Exception e) {
                // Cột đã được rename hoặc không tồn tại
            }
        }
    };

    // Migration từ version 2 lên 3 - Fix schema integrity
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Đảm bảo schema consistency
            try {
                // Kiểm tra và tạo lại bảng Transactions nếu cần
                database.execSQL("DROP TABLE IF EXISTS Transactions");
                database.execSQL("CREATE TABLE Transactions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "userId INTEGER NOT NULL, " +
                        "transactionType TEXT, " +
                        "amount REAL, " +
                        "transactionDate INTEGER, " +
                        "description TEXT, " +
                        "relatedUserId INTEGER, " +
                        "status TEXT, " +
                        "orderId INTEGER, " +
                        "auctionId INTEGER, " +
                        "referenceId TEXT, " +
                        "createdAt INTEGER, " +
                        "updatedAt INTEGER, " +
                        "FOREIGN KEY(userId) REFERENCES user(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY(relatedUserId) REFERENCES user(id) ON DELETE SET NULL, " +
                        "FOREIGN KEY(orderId) REFERENCES `order`(id) ON DELETE SET NULL, " +
                        "FOREIGN KEY(auctionId) REFERENCES auctions(id) ON DELETE SET NULL" +
                        ")");
            } catch (Exception e) {
                // Log error nếu cần
            }
        }
    };

    // Migration từ version 3 lên 4 - Giữ nguyên ChatCommunity
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Không thay đổi gì, chỉ giữ nguyên cấu trúc hiện tại
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "safezone_database")
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                            .fallbackToDestructiveMigration() // Xóa database cũ nếu migration thất bại
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Method để clear database khi cần thiết
    public static void clearDatabase(Context context) {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
        // Xóa file database
        context.deleteDatabase("safezone_database");
    }

    // Method để force reinitialize database với dữ liệu mới
    public static void forceReinitialize(Context context) {
        clearDatabase(context);
        getDatabase(context); // Sẽ tạo database mới với dữ liệu mới
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Khởi tạo dữ liệu mẫu khi database được tạo lần đầu
            DatabaseInitializer.populateAsync(INSTANCE);
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Đảm bảo có dữ liệu mẫu khi mở lại app
            DatabaseInitializer.ensureSeedDataAsync(INSTANCE);
        }
    };
}