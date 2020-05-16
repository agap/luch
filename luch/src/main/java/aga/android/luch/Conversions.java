package aga.android.luch;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import static java.lang.Character.digit;

public class Conversions {

    private static final char[] HEX_ARRAY = {
        '0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'A', 'B',
        'C', 'D', 'E', 'F'
    };

    private Conversions() {

    }

    public static byte[] uuidStringToByteArray(@NonNull String uuid) {
        final String hex = uuid.replace("-", "");

        return hexStringToByteArray(hex);
    }

    public static byte[] hexStringToByteArray(@NonNull String hexString) {
        final int length = hexString.length();

        final byte[] result = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            final int firstDigit = digit(hexString.charAt(i), 16) << 4;
            final int secondDigit = digit(hexString.charAt(i + 1), 16);

            result[i / 2] = (byte) (firstDigit + secondDigit);
        }

        return result;
    }

    public static String byteArrayToHexString(@NonNull byte[] bytes) {
        return byteArrayToHexStringBuilder(bytes).toString();
    }

    private static StringBuilder byteArrayToHexStringBuilder(@NonNull byte[] bytes) {
        final StringBuilder stringBuilder = new StringBuilder(bytes.length * 2 + 4);

        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            stringBuilder.insert(j * 2, HEX_ARRAY[v >>> 4]);
            stringBuilder.insert(j * 2 + 1, HEX_ARRAY[v & 0x0F]);
        }

        return stringBuilder;
    }

    public static List<Byte> asList(@NonNull byte[] bytes) {
        final List<Byte> list = new ArrayList<>();

        for (Byte singleByte : bytes) {
            list.add(singleByte);
        }

        return list;
    }

    public static byte[] asByteArray(@NonNull List<Byte> bytes) {
        final byte[] array = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++) {
            array[i] = bytes.get(i);
        }

        return array;
    }
}
