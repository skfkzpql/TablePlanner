package com.hyunn.tableplanner.util;

import com.hyunn.tableplanner.exception.PasswordException;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    public static String encryptPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new PasswordException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty() || hashedPassword == null || hashedPassword.isEmpty()) {
            throw new PasswordException("Password or hashed password cannot be null or empty");
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
