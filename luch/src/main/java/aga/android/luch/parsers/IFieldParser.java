package aga.android.luch.parsers;

import java.util.List;

import androidx.annotation.NonNull;

public interface IFieldParser<T> {

    /**
     * Parses a given part of an advertisement package as a field of type T starting from the
     * start byte
     * @param packet beacon's advertisement package
     * @return parsed data
     */
    T consume(@NonNull List<Byte> packet);

    // todo add unit tests
    void insert(@NonNull List<Byte> packet, @NonNull T value);

    // todo add unit tests
    void insertMask(@NonNull List<Byte> packet, byte maskBit);

    /**
     * Used to check if the parser will be able to parse the value of type clazz
     * @param clazz the type to check against
     * @return true if the parser can be used to parse the value having that type, false otherwise
     */
    // todo add unit tests
    boolean canParse(@NonNull Class clazz);
}
