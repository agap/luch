package aga.android.luch;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import static java.lang.Character.digit;

class Conversions {

    private static final char[] HEX_ARRAY = {
        '0', '1', '2', '3',
        '4', '5', '6', '7',
        '8', '9', 'A', 'B',
        'C', 'D', 'E', 'F'
    };

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

    static String byteArrayToUuidString(@NonNull @Size(min = 16, max = 16) byte[] uuid) {
        final StringBuilder stringBuilder = new StringBuilder(uuid.length * 2 + 4);

        for (int j = 0; j < uuid.length; j++) {
            final int v = uuid[j] & 0xFF;
            stringBuilder.insert(j * 2, HEX_ARRAY[v >>> 4]);
            stringBuilder.insert(j * 2 + 1, HEX_ARRAY[v & 0x0F]);
        }

        stringBuilder.insert(8, '-');
        stringBuilder.insert(13, '-');
        stringBuilder.insert(18, '-');
        stringBuilder.insert(23, '-');

        return stringBuilder.toString();
    }

    static int byteArrayToInteger(@NonNull @Size(min = 2, max = 2) byte[] byteArray) {
        return (byteArray[0] & 0xff) * 0x100 + (byteArray[1] & 0xff);
    }

    static byte[] integerToByteArray(int value) {
        return new byte[] {
            (byte) (value / 256),
            (byte) (value % 256)
        };
    }
}
