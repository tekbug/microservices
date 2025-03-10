package com.athena.v2.libraries.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class IdGenerator {
    public static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRTSUVWXYZ123456789";

    public static final int ID_LENGTH = 3;

    public static String generateUserId(String prefix) {

        StringBuilder builder = new StringBuilder();
        Random random = new Random();

        builder.append(prefix).append("-");

        for (int i = 0; i < ID_LENGTH; i++) {
            int index = random.nextInt(ALPHA_NUMERIC.length());
            builder.append(ALPHA_NUMERIC.charAt(index));
        }

        return builder.toString();
    }
}
