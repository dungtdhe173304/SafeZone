package com.group5.safezone.model.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(tableName = "verify",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "UserId",
                onDelete = ForeignKey.CASCADE))
public class Verify {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "CCCDFront")
    private String cccdFront;

    @ColumnInfo(name = "CCCDBack")
    private String cccdBack;

    @ColumnInfo(name = "bank")
    private String bank;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "numberBank")
    private String numberBank;

    @ColumnInfo(name = "securityCode")
    private String securityCode;

    @ColumnInfo(name = "UserId")
    private int userId;

    // Constructor
    public Verify() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCccdFront() { return cccdFront; }
    public void setCccdFront(String cccdFront) { this.cccdFront = cccdFront; }

    public String getCccdBack() { return cccdBack; }
    public void setCccdBack(String cccdBack) { this.cccdBack = cccdBack; }

    public String getBank() { return bank; }
    public void setBank(String bank) { this.bank = bank; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNumberBank() { return numberBank; }
    public void setNumberBank(String numberBank) { this.numberBank = numberBank; }

    public String getSecurityCode() { return securityCode; }
    public void setSecurityCode(String securityCode) { this.securityCode = securityCode; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
