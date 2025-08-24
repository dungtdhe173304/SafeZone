package com.group5.safezone.config;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
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
}, version = 1, exportSchema = false)
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

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "safezone_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
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
