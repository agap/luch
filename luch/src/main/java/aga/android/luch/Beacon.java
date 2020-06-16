package aga.android.luch;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import aga.android.luch.distance.AbstractDistanceCalculator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Beacon {

    @NonNull
    private final String hardwareAddress;

    @NonNull
    private final List beaconIdentifiers;

    @Nullable
    private final AbstractDistanceCalculator distanceCalculator;

    private byte rssi;

    private long lastSeenAtSystemClock;

    public Beacon(@NonNull String hardwareAddress,
                  @NonNull List beaconIdentifiers,
                  @Nullable AbstractDistanceCalculator distanceCalculator) {
        this.hardwareAddress = hardwareAddress;
        this.beaconIdentifiers = beaconIdentifiers;
        this.distanceCalculator = distanceCalculator;
    }

    @Nullable
    public UUID getIdentifierAsUuid(int index) {
        return ((UUID) beaconIdentifiers.get(index));
    }

    public int getIdentifierAsInt(int index) {
        return ((Integer) beaconIdentifiers.get(index));
    }

    public byte getIdentifierAsByte(int index) {
        return (byte) beaconIdentifiers.get(index);
    }

    public byte getRssi() {
        return rssi;
    }

    void setRssi(byte rssi) {
        this.rssi = rssi;
    }

    //todo test me
    public double getDistance() {
        if (distanceCalculator == null) {
            return Double.MAX_VALUE;
        } else {
            return distanceCalculator.getDistance(this);
        }
    }

    @NonNull
    public String getHardwareAddress() {
        return hardwareAddress;
    }

    long getLastSeenAtSystemClock() {
        return lastSeenAtSystemClock;
    }

    void setLastSeenAtSystemClock(long lastSeenAtSystemClock) {
        this.lastSeenAtSystemClock = lastSeenAtSystemClock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Beacon beacon = (Beacon) o;
        return hardwareAddress.equals(beacon.hardwareAddress)
            && beaconIdentifiers.equals(beacon.beaconIdentifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hardwareAddress, beaconIdentifiers);
    }

    @NonNull
    @Override
    public String toString() {
        return "Beacon{"
            + "hardwareAddress='" + hardwareAddress + '\''
            + ", rssi=" + rssi
            + ", beaconIdentifiers=" + beaconIdentifiers
            + '}';
    }
}
