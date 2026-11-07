package ru.zidan.rbpo.security;

import java.nio.charset.StandardCharsets;

public class PasswordRules {
    public static boolean valid(String password) {
        if (password == null) {
            return false;
        }
        if (password.length() < 12) {
            return false;
        }
        // У BCrypt ограничение именно в байтах.
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return false;
        }
        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean special = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else if (!Character.isWhitespace(c)) {
                special = true;
            }
        }
        return upper && lower && digit && special;
    }
}
