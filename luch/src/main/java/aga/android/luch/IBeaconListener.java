package aga.android.luch;

import java.util.Collection;

import androidx.annotation.NonNull;

public interface IBeaconListener {

    void onNearbyBeaconsDetected(@NonNull Collection<Beacon> beacons);
}
