package aga.android.luch.distance;

import java.util.List;

import aga.android.luch.Beacon;
import aga.android.luch.ITimeProvider;
import androidx.annotation.NonNull;

import static java.lang.Math.pow;

public final class Ranger {

    private final IRssiFilter rssiFilter;

    private final ReadingsCache cache;

    public Ranger(ITimeProvider timeProvider,
                  IRssiFilter rssiFilter) {
        this(new ReadingsCache(timeProvider), rssiFilter);
    }

    Ranger(ReadingsCache readingsCache,
           IRssiFilter rssiFilter) {
        this.cache = readingsCache;
        this.rssiFilter = rssiFilter;
    }

    /**
     *  Simple distance calculator based on the paper called "Evaluation of the Reliability of RSSI
     *  for Indoor Localization".
     *  See here: https://www.rn.inf.tu-dresden.de/dargie/papers/icwcuca.pdf
     * @param beacon
     * @return calculated distance
     */
    public double calculateDistance(@NonNull Beacon beacon) {
        final List<Reading> beaconReadings = cache.getReadingsOf(beacon);

        final byte rssi = beaconReadings.isEmpty()
            ? beacon.getRssi()
            : rssiFilter.getFilteredValue(beaconReadings);

        final Byte txPower = beacon.getTxPower();

        return txPower != null
            ? pow(10.0, (float) (txPower - rssi) / 20)
            : Double.MAX_VALUE;
    }

    public void addReading(@NonNull Beacon beacon, byte rssi) {
        cache.add(beacon, rssi);
    }

    public void removeReadings(@NonNull Beacon beacon) {
        cache.remove(beacon);
    }

    public void trim() {
        cache.trim();
    }
}
