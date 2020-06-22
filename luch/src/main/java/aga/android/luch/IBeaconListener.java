package aga.android.luch;

import androidx.annotation.NonNull;

/**
 * Notifies the subscribers about enter and exit events for any given beacon.
 */
public interface IBeaconListener {

    /**
     * Called when the new beacon is detected.
     * @param beacon a newly detected beacon
     */
    void onBeaconEntered(@NonNull Beacon beacon);

    /**
     * Called when the state of previously detected beacon was updated.
     * One example of such call is when the current RSSI value is adjusted because we received a
     * new advertisement package.
     * @param beacon the previously detected beacon.
     */
    void onBeaconUpdated(@NonNull Beacon beacon);

    /**
     * Called when the previously detected beacon was not seen for more than N seconds (see
     * {@link BeaconScanner.Builder#setBeaconExpirationDuration(long)})
     * @param beacon the lost beacon
     */
    void onBeaconExited(@NonNull Beacon beacon);
}
