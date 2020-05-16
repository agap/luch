package aga.android.luch.parsers;

/**
 * An exception that can be thrown if the relevant IFieldParser implementation could not
 * produce the relevant part of BLE packet scan/filter during the RegionDefinition -> ScanFilter
 * mapping.
 */
class RegionDefinitionConversionException extends Exception {

    RegionDefinitionConversionException(String message) {
        super(message);
    }
}
