package aga.android.luch.distance;

import java.util.List;

import androidx.annotation.NonNull;

public final class RunningAverageRssiFilter implements IRssiFilter {

    @Override
    public byte getFilteredValue(@NonNull List<Reading> readings) {
        int rssiSum = 0;

        for (Reading reading : readings) {
            rssiSum += reading.rssi;
        }

        return (byte) (rssiSum / readings.size());
    }
}