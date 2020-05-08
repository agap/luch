package aga.android.luch.parsers;

import androidx.annotation.NonNull;

public interface IFieldParser<T> {

    /**
     * Parses a given part of an advertisement package as a field of type T starting from the
     * start byte
     * @param packet beacon's advertisement package
     * @param start the index of the first byte of a given field in the advertisement package
     * @return parsed data
     * @throws BeaconParseException in case the parsing could not succeed
     */
    T parse(@NonNull byte[] packet, int start) throws BeaconParseException;

    /**
     * Returns the field length to be used during the construction of
     * a {@link android.bluetooth.le.ScanFilter} object
     * @return length
     */
    int getFieldLength();
}
