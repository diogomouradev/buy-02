package com.gritlab.buy01.userservice.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {
    private static final Pattern emailRegexPattern = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
            , Pattern.CASE_INSENSITIVE);

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = emailRegexPattern.matcher(email);
        return matcher.matches();
    }
}
