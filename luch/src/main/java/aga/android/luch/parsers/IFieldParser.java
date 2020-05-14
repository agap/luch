package aga.android.luch.parsers;

import java.util.List;

import androidx.annotation.NonNull;

public interface IFieldParser<T> {

    /**
     * Parses a given part of an advertisement package as a field of type T starting from the
     * start byte
     * @param packet beacon's advertisement package
     * @return parsed data
     * @throws BeaconParseException in case the parsing could not succeed
     */
    T consume(@NonNull List<Byte> packet) throws BeaconParseException;

    void insert(@NonNull List<Byte> packet, @NonNull T value);

    void insertMask(@NonNull List<Byte> packet, byte maskBit);

    /**
     * Used to check if the parser will be able to parse the value of type clazz
     * @param clazz the type to check against
     * @return true if the parser can be used to parse the value having that type, false otherwise
     */
    boolean canParse(@NonNull Class clazz);
}
