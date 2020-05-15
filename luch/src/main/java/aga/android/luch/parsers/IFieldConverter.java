package aga.android.luch.parsers;

import java.util.List;

import androidx.annotation.NonNull;

interface IFieldConverter<T> {

    /**
     * Parses a part of an advertisement package as a field of type T starting from the
     * first (zero-indexed) byte.
     *
     * The method's name is chosen to signify the fact that the incoming packet of data will
     * be smaller once {@link IFieldConverter} is done consuming all the bytes that it needs. How
     * many bytes will be consumed is defined by each {@link IFieldConverter}.
     *
     * @param packet beacon's advertisement package
     * @return parsed data
     */
    T consume(@NonNull List<Byte> packet);

    /**
     * Converts the given value into a sequence of {@link Byte}s and appends them to the packet one
     * after another.
     *
     * @param packet the list of bytes to append the aforementioned bytes to
     * @param value the value to be converted into a sequence of {@link Byte}s
     */
    void insert(@NonNull List<Byte> packet, @NonNull T value);

    /**
     * Appends the mask bits (either 0 or 1) to the given mask packet. The amount of bytes to be
     * appended is defined by each {@link IFieldConverter}, but is has to match the size of the
     * sequence of bytes produced by the insert method out of the value of type T.
     * @param packet the mask packet to append the mask bits to
     * @param maskBit the mask bit value (either 0 or 1)
     */
    void insertMask(@NonNull List<Byte> packet, byte maskBit);

    /**
     * Used to check if the parser will be able to parse the value of type clazz
     * @param clazz the type to check against
     * @return true if the parser can be used to parse the value having that type, false otherwise
     */
    // todo add unit tests
    boolean canParse(@NonNull Class clazz);
}
