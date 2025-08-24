package com.group5.safezone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.PopupMenu;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.group5.safezone.R;
import com.group5.safezone.model.entity.Product;
import com.group5.safezone.model.entity.ProductImages;
import com.group5.safezone.util.ProductDisplayHelper;
import androidx.annotation.Nullable;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;
    private List<List<ProductImages>> productImagesList;
    private Map<Integer, String> userNames;
    private int currentUserId;
    private Map<String, Boolean> productSoldStatus; // Track which products are sold

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onDeleteClick(Product product);
        void onEditClick(Product product);
    }

    public ProductAdapter(List<Product> products, List<List<ProductImages>> productImagesList, Map<Integer, String> userNames, int currentUserId, OnProductClickListener listener) {
        this.products = products;
        this.productImagesList = productImagesList;
        this.userNames = userNames;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.productSoldStatus = new java.util.HashMap<>();
    }

    public void updateData(List<Product> products, List<List<ProductImages>> productImagesList, Map<Integer, String> userNames) {
        this.products = products;
        this.productImagesList = productImagesList;
        this.userNames = userNames;
        notifyDataSetChanged();
    }
    
    public void updateProductSoldStatus(Map<String, Boolean> soldStatus) {
        this.productSoldStatus = soldStatus != null ? soldStatus : new java.util.HashMap<>();
        notifyDataSetChanged();
    }
    
    public void updateCurrentUserId(int newCurrentUserId) {
        this.currentUserId = newCurrentUserId;
        System.out.println("=== ProductAdapter: Updated currentUserId to: " + newCurrentUserId + " ===");
        notifyDataSetChanged(); // Refresh to update menu visibility
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        List<ProductImages> images = productImagesList != null && position < productImagesList.size() 
            ? productImagesList.get(position) : null;
        holder.bind(product, images);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    class ProductViewHolder extends ViewHolder {
        private TextView tvProductName, tvDescription, tvFee, tvPrice, tvStatus, tvPublicPrivate, tvSellerUsername, tvTimePosted, tvViews;
        private ImageButton btnMenu;
        private Button btnViewDetails;
        private ImageView ivSellerAvatar;
        private RecyclerView rvProductImages;
        private ProductImagesAdapter imagesAdapter;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvFee = itemView.findViewById(R.id.tvFee);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPublicPrivate = itemView.findViewById(R.id.tvPublicPrivate);
            tvSellerUsername = itemView.findViewById(R.id.tvSellerUsername);
            tvTimePosted = itemView.findViewById(R.id.tvTimePosted);
            tvViews = itemView.findViewById(R.id.tvViews);
            btnMenu = itemView.findViewById(R.id.btnMenu);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            ivSellerAvatar = itemView.findViewById(R.id.ivSellerAvatar);
            rvProductImages = itemView.findViewById(R.id.rvProductImages);
            
            // Debug: Check if btnMenu is found
            if (btnMenu != null) {
                System.out.println("=== ProductAdapter: btnMenu found successfully ===");
            } else {
                System.out.println("=== ProductAdapter: ERROR - btnMenu is NULL! ===");
            }

            // Setup product images RecyclerView
            rvProductImages.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            imagesAdapter = new ProductImagesAdapter();
            rvProductImages.setAdapter(imagesAdapter);

            // Setup click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });

            btnViewDetails.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });



            // Setup menu button click listener
            if (btnMenu != null) {
                btnMenu.setOnClickListener(v -> {
                    System.out.println("=== ProductAdapter: Menu button clicked ===");
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Product product = products.get(position);
                        System.out.println("=== ProductAdapter: Showing menu for product at position: " + position + " ===");
                        System.out.println("=== Product name: " + product.getProductName() + " ===");
                        showProductMenu(v, product);
                    } else {
                        System.out.println("=== ProductAdapter: Invalid position: " + position + " ===");
                    }
                });
                System.out.println("=== ProductAdapter: Menu button click listener set successfully ===");
            } else {
                System.out.println("=== ProductAdapter: ERROR - Cannot set click listener, btnMenu is NULL! ===");
            }
        }

        public void bind(Product product, List<ProductImages> images) {
            System.out.println("=== ProductAdapter: Binding product: " + product.getProductName() + " ===");
            System.out.println("=== ProductAdapter: btnMenu status: " + (btnMenu != null ? "NOT NULL" : "NULL") + " ===");
            
            tvProductName.setText(product.getProductName());
            
            // Set description (handle null case)
            String description = product.getDescribe();
            if (description != null && !description.trim().isEmpty()) {
                tvDescription.setText(description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            
            // Set fee with proper formatting
            tvFee.setText(ProductDisplayHelper.formatFee(product.getFee()));
            
            tvPrice.setText(ProductDisplayHelper.formatPrice(product.getPrice()));
            tvStatus.setText("Status: " + (product.getStatus() != null ? product.getStatus() : "Active"));
            tvViews.setText(ProductDisplayHelper.formatViews(product.getView()));
            
            // Set seller username from userNames map
            String username = userNames != null ? userNames.get(product.getUserId()) : null;
            if (username != null) {
                tvSellerUsername.setText(username);
            } else {
                tvSellerUsername.setText("User " + product.getUserId());
            }
            
            // Set time posted
            tvTimePosted.setText(ProductDisplayHelper.formatTimeAgo(product.getCreatedAt()));
            
            // Set public/private status
            String publicPrivate = product.getPublicPrivate();
            if ("public".equals(publicPrivate)) {
                tvPublicPrivate.setText(itemView.getContext().getString(R.string.visibility_public));
                tvPublicPrivate.setBackgroundResource(R.drawable.public_status_background);
                tvPublicPrivate.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                tvPublicPrivate.setText(itemView.getContext().getString(R.string.visibility_private));
                tvPublicPrivate.setBackgroundResource(R.drawable.private_status_background);
                tvPublicPrivate.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
            }

            // Show menu button for all users
            if (btnMenu != null) {
                btnMenu.setVisibility(View.VISIBLE);
                System.out.println("=== ProductAdapter: Menu button visibility set to VISIBLE for product: " + product.getProductName() + " ===");
                System.out.println("=== ProductAdapter: btnMenu actual visibility: " + (btnMenu.getVisibility() == View.VISIBLE ? "VISIBLE" : "NOT VISIBLE") + " ===");
            } else {
                System.out.println("=== ProductAdapter: ERROR - Cannot set visibility, btnMenu is NULL! ===");
            }

            // Load product images
            if (images != null && !images.isEmpty()) {
                System.out.println("Product " + product.getProductName() + " has " + images.size() + " images");
                imagesAdapter.updateImages(images);
                rvProductImages.setVisibility(View.VISIBLE);
            } else {
                System.out.println("Product " + product.getProductName() + " has no images");
                rvProductImages.setVisibility(View.GONE);
            }

            // Load seller avatar (using default person icon for now)
            ivSellerAvatar.setImageResource(R.drawable.ic_person);
        }

        private void showProductMenu(View anchor, Product product) {
            System.out.println("=== ProductAdapter: Showing menu for product: " + product.getProductName() + " ===");
            System.out.println("=== Product User ID: " + product.getUserId() + " ===");
            System.out.println("=== Current User ID: " + currentUserId + " ===");
            System.out.println("=== Is Owner: " + (product.getUserId() == currentUserId) + " ===");
            System.out.println("=== Menu button clicked at position: " + getAdapterPosition() + " ===");
            
            PopupMenu popup = new PopupMenu(anchor.getContext(), anchor);
            popup.inflate(R.menu.product_menu);
            
            // Check if product is sold
            boolean isProductSold = productSoldStatus != null && productSoldStatus.get(product.getId()) != null && productSoldStatus.get(product.getId());
            
            // Show/hide menu items based on ownership and sold status
            if (product.getUserId() == currentUserId) {
                // Product owner can see all options, but Edit/Delete are hidden if product is sold
                System.out.println("=== ProductAdapter: User is OWNER - showing menu items ===");
                System.out.println("=== Product is sold: " + isProductSold + " ===");
                
                popup.getMenu().findItem(R.id.action_view).setVisible(true);
                popup.getMenu().findItem(R.id.action_edit).setVisible(!isProductSold);
                popup.getMenu().findItem(R.id.action_delete).setVisible(!isProductSold);
                popup.getMenu().findItem(R.id.action_copy_link).setVisible(true);
                
                if (isProductSold) {
                    System.out.println("=== ProductAdapter: Product is SOLD - hiding Edit/Delete for owner ===");
                } else {
                    System.out.println("=== ProductAdapter: Product is NOT sold - showing Edit/Delete for owner ===");
                }
            } else {
                // Non-owner can only view and copy link
                System.out.println("=== ProductAdapter: User is NOT owner - hiding edit/delete ===");
                popup.getMenu().findItem(R.id.action_view).setVisible(true);
                popup.getMenu().findItem(R.id.action_delete).setVisible(false);
                popup.getMenu().findItem(R.id.action_edit).setVisible(false);
                popup.getMenu().findItem(R.id.action_copy_link).setVisible(true);
            }
            
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                System.out.println("=== ProductAdapter: Menu item clicked: " + itemId + " ===");
                
                if (itemId == R.id.action_view) {
                    System.out.println("=== ProductAdapter: View clicked for product: " + product.getProductName() + " ===");
                    if (listener != null) {
                        listener.onProductClick(product);
                    }
                    return true;
                } else if (itemId == R.id.action_edit) {
                    System.out.println("=== ProductAdapter: Edit clicked for product: " + product.getProductName() + " ===");
                    if (listener != null) {
                        listener.onEditClick(product);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    System.out.println("=== ProductAdapter: Delete clicked for product: " + product.getProductName() + " ===");
                    if (listener != null) {
                        listener.onDeleteClick(product);
                    }
                    return true;
                } else if (itemId == R.id.action_copy_link) {
                    System.out.println("=== ProductAdapter: Copy link clicked for product: " + product.getProductName() + " ===");
                    copyProductLink(product);
                    return true;
                }
                return false;
            });
            
            popup.show();
        }

        private void copyProductLink(Product product) {
            // Create a deep link to the product
            String productLink = "safezone://product/" + product.getId();
            
            // Copy to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                itemView.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Product Link", productLink);
            clipboard.setPrimaryClip(clip);
            
            // Show toast
            android.widget.Toast.makeText(itemView.getContext(), 
                "Đã copy link sản phẩm", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    // Inner adapter for product images
    public static class ProductImagesAdapter extends RecyclerView.Adapter<ProductImagesAdapter.ImageViewHolder> {
        private List<ProductImages> images;
        private OnImageClickListener imageClickListener;

        public interface OnImageClickListener {
            void onImageClick(ProductImages image, int position);
        }

        public void setOnImageClickListener(OnImageClickListener listener) {
            this.imageClickListener = listener;
        }

        public void updateImages(List<ProductImages> images) {
            this.images = images;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            
            // Set image size to be square and fit the RecyclerView height
            int imageSize = 200; // dp equivalent
            
            // Create MarginLayoutParams directly instead of casting
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.MATCH_PARENT
            );
            
            // Set margins for spacing between images
            params.setMargins(8, 8, 8, 8);
            
            imageView.setLayoutParams(params);
            
            // Set image properties
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
            imageView.setMinimumWidth(imageSize);
            imageView.setMinimumHeight(imageSize);
            
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            ProductImages image = images.get(position);
            if (image.getPath() != null && !image.getPath().isEmpty()) {
                // Debug: Log the image path
                System.out.println("Loading image: " + image.getPath());
                
                // Check if path is a valid file path or URI
                String imagePath = image.getPath();
                Object imageSource = imagePath;
                
                // If it's a resource ID (starts with android.resource://), try to parse it
                if (imagePath.startsWith("android.resource://")) {
                    try {
                        // Extract resource ID from URI
                        String[] parts = imagePath.split("/");
                        if (parts.length > 0) {
                            int resourceId = Integer.parseInt(parts[parts.length - 1]);
                            imageSource = resourceId;
                            System.out.println("Parsed resource ID: " + resourceId);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid resource ID format: " + imagePath);
                        imageSource = R.drawable.ic_info; // Fallback to placeholder
                    }
                }
                
                // Load image using Glide with error handling
                Glide.with(holder.imageView.getContext())
                    .load(imageSource)
                    .placeholder(R.drawable.ic_info)
                    .error(R.drawable.ic_info)
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            System.out.println("Glide load failed for: " + model + ", Error: " + (e != null ? e.getMessage() : "Unknown"));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            System.out.println("Glide load successful for: " + model);
                            return false;
                        }
                    })
                    .into(holder.imageView);
            } else {
                // If no path, show placeholder
                holder.imageView.setImageResource(R.drawable.ic_info);
                System.out.println("No image path for position: " + position);
            }

            // Set click listener for image
            holder.imageView.setOnClickListener(v -> {
                if (imageClickListener != null) {
                    imageClickListener.onImageClick(image, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return images != null ? images.size() : 0;
        }

        static class ImageViewHolder extends ViewHolder {
            ImageView imageView;

            ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}
