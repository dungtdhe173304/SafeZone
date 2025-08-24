package com.group5.safezone.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PasswordUtils {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SALT_SEPARATOR = ":";
    
    /**
     * Hash password với salt (cho mật khẩu mới)
     */
    public static String hashPassword(String password) {
        try {
            // Tạo salt ngẫu nhiên
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            // Hash password + salt
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Kết hợp salt và hash
            StringBuilder sb = new StringBuilder();
            sb.append(bytesToHex(salt));
            sb.append(SALT_SEPARATOR);
            sb.append(bytesToHex(hashedPassword));
            
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }
    
    /**
     * Hash password cũ (không có salt) - để tương thích ngược
     */
    public static String hashPasswordOld(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashedBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }
    
    /**
     * Verify password - hỗ trợ cả mật khẩu cũ và mới
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Kiểm tra xem có phải mật khẩu mới (có salt) không
            if (storedHash.contains(SALT_SEPARATOR)) {
                return verifyPasswordWithSalt(password, storedHash);
            } else {
                // Mật khẩu cũ (không có salt)
                return verifyPasswordOld(password, storedHash);
            }
            
        } catch (Exception e) {
            // Nếu có lỗi, thử verify theo cách cũ
            return verifyPasswordOld(password, storedHash);
        }
    }
    
    /**
     * Verify password với salt (mật khẩu mới)
     */
    private static boolean verifyPasswordWithSalt(String password, String storedHash) {
        try {
            // Tách salt và hash
            String[] parts = storedHash.split(SALT_SEPARATOR);
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = hexToBytes(parts[0]);
            byte[] storedHashBytes = hexToBytes(parts[1]);
            
            // Hash password với salt đã lưu
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] inputHash = md.digest(password.getBytes());
            
            // So sánh hash
            return MessageDigest.isEqual(storedHashBytes, inputHash);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }
    
    /**
     * Verify password cũ (không có salt)
     */
    private static boolean verifyPasswordOld(String password, String storedHash) {
        try {
            String hashedInput = hashPasswordOld(password);
            return hashedInput.equals(storedHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Chuyển đổi byte array thành hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Chuyển đổi hex string thành byte array
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return bytes;
    }
    
    /**
     * Tạo mật khẩu ngẫu nhiên
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Kiểm tra xem mật khẩu có cần được cập nhật lên format mới không
     */
    public static boolean needsUpgrade(String storedHash) {
        return !storedHash.contains(SALT_SEPARATOR);
    }
    
    /**
     * Cập nhật mật khẩu từ format cũ lên format mới
     */
    public static String upgradePassword(String password, String oldHash) {
        // Verify mật khẩu cũ trước
        if (verifyPasswordOld(password, oldHash)) {
            // Nếu đúng, hash lại với salt mới
            return hashPassword(password);
        }
        return oldHash; // Giữ nguyên nếu verify thất bại
    }

    // Simple encryption for private info - using a simpler approach
    private static final String SECRET_KEY = "SafeZoneSecretKey1234567890123456"; // Must be 16, 24, or 32 bytes for AES
    
    /**
     * Mã hóa dữ liệu nhạy cảm
     */
    public static String encrypt(String data) throws Exception {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        try {
            // Ensure key is exactly 16 bytes for AES-128
            byte[] keyBytes = SECRET_KEY.getBytes("UTF-8");
            if (keyBytes.length > 16) {
                byte[] newKeyBytes = new byte[16];
                System.arraycopy(keyBytes, 0, newKeyBytes, 0, 16);
                keyBytes = newKeyBytes;
            } else if (keyBytes.length < 16) {
                byte[] newKeyBytes = new byte[16];
                System.arraycopy(keyBytes, 0, newKeyBytes, 0, keyBytes.length);
                // Fill remaining bytes with zeros
                for (int i = keyBytes.length; i < 16; i++) {
                    newKeyBytes[i] = 0;
                }
                keyBytes = newKeyBytes;
            }
            
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            // Use AES/ECB/PKCS5Padding for simplicity (in production, use CBC mode with IV)
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            
            // Return base64 encoded string
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new Exception("Encryption failed: " + e.getMessage());
        }
    }
    
    /**
     * Giải mã dữ liệu đã được mã hóa
     */
    public static String decrypt(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        
        try {
            // Ensure key is exactly 16 bytes for AES-128
            byte[] keyBytes = SECRET_KEY.getBytes("UTF-8");
            if (keyBytes.length > 16) {
                byte[] newKeyBytes = new byte[16];
                System.arraycopy(keyBytes, 0, newKeyBytes, 0, 16);
                keyBytes = newKeyBytes;
            } else if (keyBytes.length < 16) {
                byte[] newKeyBytes = new byte[16];
                System.arraycopy(keyBytes, 0, newKeyBytes, 0, keyBytes.length);
                // Fill remaining bytes with zeros
                for (int i = keyBytes.length; i < 16; i++) {
                    newKeyBytes[i] = 0;
                }
                keyBytes = newKeyBytes;
            }
            
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            
            // Use AES/ECB/PKCS5Padding for simplicity
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new Exception("Decryption failed: " + e.getMessage());
        }
    }
}
