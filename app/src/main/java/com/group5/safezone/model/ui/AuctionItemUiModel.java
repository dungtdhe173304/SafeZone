package com.group5.safezone.model.ui;

import com.group5.safezone.model.entity.AuctionRegistrations;
import com.group5.safezone.model.entity.Auctions;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;

import java.util.List;

public class AuctionItemUiModel {
    private final Product product;
    private final Auctions auction;
    private final List<ProductImages> images;
    private final boolean isRegistered;
    private final AuctionRegistrations registration;
    private final int participantCount;
    private final String sellerUserName;
    private final String sellerEmail;
    private final String sellerPhone;

    public AuctionItemUiModel(Product product,
                              Auctions auction,
                              List<ProductImages> images,
                              boolean isRegistered,
                              AuctionRegistrations registration,
                              int participantCount,
                              String sellerUserName,
                              String sellerEmail,
                              String sellerPhone) {
        this.product = product;
        this.auction = auction;
        this.images = images;
        this.isRegistered = isRegistered;
        this.registration = registration;
        this.participantCount = participantCount;
        this.sellerUserName = sellerUserName;
        this.sellerEmail = sellerEmail;
        this.sellerPhone = sellerPhone;
    }

    public Product getProduct() {
        return product;
    }

    public Auctions getAuction() {
        return auction;
    }

    public List<ProductImages> getImages() {
        return images;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public AuctionRegistrations getRegistration() {
        return registration;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public String getSellerUserName() {
        return sellerUserName;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }
}


