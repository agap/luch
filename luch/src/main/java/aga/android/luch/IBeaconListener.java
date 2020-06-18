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
     * Called when the previously detected beacon was not seen for more than N seconds (see
     * {@link BeaconScanner.Builder#setBeaconExpirationDuration(long)})
     * @param beacon the lost beacon
     */
    void onBeaconExited(@NonNull Beacon beacon);
}
