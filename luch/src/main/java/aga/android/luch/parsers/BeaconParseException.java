package aga.android.luch.parsers;

/**
 * An exception that can be thrown if the relevant IFieldParser implementation could not
 * correctly parse the data from the beacon advertisement.
 */
public class BeaconParseException extends Exception {
    public BeaconParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
