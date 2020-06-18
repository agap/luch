package aga.android.luch;

import java.util.Collection;

import androidx.annotation.NonNull;

/**
 * Notifies the subscribers about all beacons which are considered to be in close vicinity.
 */
public interface IBeaconBatchListener {

    void onBeaconsDetected(@NonNull Collection<Beacon> beacons);
}
