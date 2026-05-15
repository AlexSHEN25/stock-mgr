package co.handk.common.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

import static co.handk.common.constant.NumberConstant.ONE;
import static co.handk.common.constant.NumberConstant.SIXTEEN;
import static co.handk.common.constant.NumberConstant.TWO;

public class PasswordUtil {

    public static String encrypt(String password, String salt) {
        try {
            String str = password + salt;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == ONE) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateSalt() {
        byte[] bytes = new byte[SIXTEEN];
        new SecureRandom().nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * TWO);
        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == ONE) {
                hex.append('0');
            }
            hex.append(s);
        }
        return hex.toString();
    }
}
