package aga.android.luch.distance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import aga.android.luch.Beacon;
import aga.android.luch.ITimeProvider;
import androidx.annotation.NonNull;

import static java.util.Objects.requireNonNull;

public final class ReadingsCache {

    private static final long DEFAULT_RSSI_VALIDITY_PERIOD_MILLIS = 5_000;

    private final Map<Beacon, List<Reading>> rssiReadingsCache = new ConcurrentHashMap<>();

    private final ITimeProvider timeProvider;

    private final long readingsValidityPeriodMillis;

    ReadingsCache(long readingsValidityPeriodMillis,
                  @NonNull ITimeProvider timeProvider) {
        this.readingsValidityPeriodMillis = readingsValidityPeriodMillis;
        this.timeProvider = timeProvider;
    }

    public ReadingsCache(@NonNull ITimeProvider timeProvider) {
        this(DEFAULT_RSSI_VALIDITY_PERIOD_MILLIS, timeProvider);
    }

    public void add(@NonNull Beacon beacon, byte rssi) {
        List<Reading> beaconReadings = rssiReadingsCache.get(beacon);

        if (beaconReadings == null) {
            beaconReadings = new ArrayList<>();
            rssiReadingsCache.put(beacon, beaconReadings);
        }

        beaconReadings.add(new Reading(rssi, timeProvider.elapsedRealTimeTimeMillis()));
    }

    public void remove(@NonNull Beacon beacon) {
        rssiReadingsCache.remove(beacon);
    }

    public void trim() {
        for (Beacon beacon : rssiReadingsCache.keySet()) {
            final List<Reading> beaconReadings = rssiReadingsCache.get(beacon);

            for (Iterator<Reading> it = requireNonNull(beaconReadings).iterator(); it.hasNext();) {
                final Reading reading = it.next();

                if (reading.timestamp + readingsValidityPeriodMillis
                        < timeProvider.elapsedRealTimeTimeMillis()) {

                    it.remove();
                }
            }
        }
    }

    @NonNull
    List<Reading> getReadingsOf(@NonNull Beacon beacon) {
        final List<Reading> readings = rssiReadingsCache.get(beacon);

        return readings == null
            ? Collections.<Reading>emptyList()
            : Collections.unmodifiableList(readings);
    }
}
