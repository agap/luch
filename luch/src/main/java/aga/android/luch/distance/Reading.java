package aga.android.luch.distance;

import java.util.Objects;

final class Reading {

    final byte rssi;
    final long timestamp;

    Reading(byte rssi, long timestamp) {
        this.rssi = rssi;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reading reading = (Reading) o;
        return rssi == reading.rssi && timestamp == reading.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rssi, timestamp);
    }
}
