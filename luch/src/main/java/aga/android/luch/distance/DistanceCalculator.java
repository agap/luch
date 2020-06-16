package aga.android.luch.distance;

import aga.android.luch.Beacon;
import androidx.annotation.NonNull;

import static java.lang.Math.pow;

/**
 * Simple distance calculator based on the paper called "Evaluation of the Reliability of RSSI
 * for Indoor Localization". See here: https://www.rn.inf.tu-dresden.de/dargie/papers/icwcuca.pdf
 */
//todo test me
class DistanceCalculator extends AbstractDistanceCalculator {

    protected DistanceCalculator(int txPowerPosition) {
        super(txPowerPosition);
    }

    @Override
    public double getDistance(@NonNull Beacon beacon) {
        final byte rssi = beacon.getRssi();
        final byte txPower = getTxPower(beacon);

        return pow(10.0, (float) (txPower - rssi) / 20);
    }
}
