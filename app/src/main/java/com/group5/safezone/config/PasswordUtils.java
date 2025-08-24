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

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(hashedPassword);
    }

    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }

    // Simple encryption for private info - using a simpler approach
    private static final String SECRET_KEY = "SafeZoneSecretKey1234567890123456"; // Must be 16, 24, or 32 bytes for AES
    
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
