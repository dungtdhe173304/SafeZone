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

    public AuctionItemUiModel(Product product,
                              Auctions auction,
                              List<ProductImages> images,
                              boolean isRegistered,
                              AuctionRegistrations registration,
                              int participantCount) {
        this.product = product;
        this.auction = auction;
        this.images = images;
        this.isRegistered = isRegistered;
        this.registration = registration;
        this.participantCount = participantCount;
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
}


