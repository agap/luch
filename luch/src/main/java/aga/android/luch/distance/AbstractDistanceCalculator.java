package aga.android.luch.distance;

import aga.android.luch.Beacon;
import androidx.annotation.NonNull;

/**
 * The extensions of this class calculate the distance to the given beacon based on its rssi and
 * txPower values.
 */
public abstract class AbstractDistanceCalculator {

    private final int txPowerPosition;

    protected AbstractDistanceCalculator(int txPowerPosition) {
        this.txPowerPosition = txPowerPosition;
    }

    protected byte getTxPower(@NonNull Beacon beacon) {
        return beacon.getIdentifierAsByte(txPowerPosition);
    }

    public abstract double getDistance(@NonNull Beacon beacon);
}
