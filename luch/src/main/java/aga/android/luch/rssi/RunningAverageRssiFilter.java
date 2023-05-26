package aga.android.luch.rssi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import aga.android.luch.ITimeProvider;
import aga.android.luch.ITimeProvider.SystemTimeProvider;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

public final class RunningAverageRssiFilter extends RssiFilter {

    private final List<Reading> readings = new ArrayList<>();

    private final ITimeProvider timeProvider;

    private final long rssiValidityPeriodMillis;

    private RunningAverageRssiFilter(ITimeProvider timeProvider,
                                    long rssiValidityPeriodMillis) {
        this.timeProvider = timeProvider;
        this.rssiValidityPeriodMillis = rssiValidityPeriodMillis;
    }

    @Override
    public void addReading(byte rssi) {

        for (Iterator<Reading> it = readings.iterator(); it.hasNext();) {
            final Reading reading = it.next();

            if (reading.timestamp + rssiValidityPeriodMillis
                    < timeProvider.elapsedRealTimeTimeMillis()) {

                it.remove();
            } else {
                break;
            }
        }

        readings.add(new Reading(rssi, timeProvider.elapsedRealTimeTimeMillis()));
    }

    @Nullable
    @Override
    public Byte getFilteredValue() {
        if (readings.isEmpty()) {
            return null;
        }

        int rssiSum = 0;

        for (Reading reading : readings) {
            rssiSum += reading.rssi;
        }

        return (byte) (rssiSum / readings.size());
    }

    public static final class Builder extends RssiFilter.Builder {

        private static final long DEFAULT_RSSI_VALIDITY_PERIOD_MILLIS = 20_000;
        private static final ITimeProvider DEFAULT_TIME_PROVIDER = new SystemTimeProvider();

        private long rssiValidity = DEFAULT_RSSI_VALIDITY_PERIOD_MILLIS;

        private ITimeProvider timeProvider = DEFAULT_TIME_PROVIDER;

        public Builder() {

        }

        public Builder addRssiValidityPeriodMillis(long rssiValidity) {
            this.rssiValidity = rssiValidity;
            return this;
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
        public Builder addTimeProvider(ITimeProvider timeProvider) {
            this.timeProvider = timeProvider;
            return this;
        }

        @Override
        public RssiFilter build() {
            return new RunningAverageRssiFilter(timeProvider, rssiValidity);
        }
    }

    private static final class Reading {

        final byte rssi;
        final long timestamp;

        Reading(byte rssi, long timestamp) {
            this.rssi = rssi;
            this.timestamp = timestamp;
        }
    }
}