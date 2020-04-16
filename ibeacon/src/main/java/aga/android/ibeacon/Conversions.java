package aga.android.ibeacon;

import androidx.annotation.NonNull;

import static java.lang.Character.digit;

class Conversions {

    private Conversions() {

    }

    static byte[] uuidStringToByteArray(@NonNull String uuid) {
        final String hex = uuid.replace("-", "");

        final int length = hex.length();

        final byte[] result = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            result[i / 2] = (byte) ((digit(hex.charAt(i), 16) << 4) + digit(hex.charAt(i + 1), 16));
        }

        return result;
    }
}
