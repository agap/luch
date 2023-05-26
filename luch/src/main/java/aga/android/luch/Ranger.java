package aga.android.luch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import aga.android.luch.rssi.RssiFilter;
import androidx.annotation.NonNull;

import static java.lang.Math.pow;

public final class Ranger {

    private final RssiFilter.Builder rssiFilterBuilder;

    private final Map<Beacon, RssiFilter> cache = new ConcurrentHashMap<>();

    Ranger(RssiFilter.Builder rssiFilterBuilder) {
        this.rssiFilterBuilder = rssiFilterBuilder;
    }

    /**
     *  Simple distance calculator based on the paper called "Evaluation of the Reliability of RSSI
     *  for Indoor Localization".
     *  See here: https://www.rn.inf.tu-dresden.de/dargie/papers/icwcuca.pdf
     * @param beacon the beacon to range
     * @return calculated distance
     */
    public double calculateDistance(@NonNull Beacon beacon) {
        final RssiFilter filter = cache.get(beacon);

        final byte rssi;

        if (filter != null) {
            final Byte filteredRssi = filter.getFilteredValue();

            if (filteredRssi == null) {
                rssi = beacon.getRssi();
            } else {
                rssi = filteredRssi;
            }
        } else {
            rssi = beacon.getRssi();
        }

        final Byte txPower = beacon.getTxPower();

        return txPower != null
            ? pow(10.0, (float) (txPower - rssi) / 20)
            : Double.MAX_VALUE;
    }

    void addReading(@NonNull Beacon beacon, byte rssi) {
        RssiFilter filter = cache.get(beacon);

        if (filter == null) {
            filter = rssiFilterBuilder.build();
            cache.put(beacon, filter);
        }

        filter.addReading(rssi);
    }

    void removeReadings(@NonNull Beacon beacon) {
        cache.remove(beacon);
    }
}
