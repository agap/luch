package aga.android.luch.parsers;

/**
 * An exception that can be thrown if the relevant IFieldParser implementation could not
 * produce the relevant part of BLE packet scan/filter during the Region -> ScanFilter
 * mapping.
 */
class RegionConversionException extends Exception {

    RegionConversionException(String message) {
        super(message);
    }
}
