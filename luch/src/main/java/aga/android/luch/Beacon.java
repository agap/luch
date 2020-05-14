package aga.android.luch;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Beacon implements IBeacon {

    @NonNull
    @Deprecated
    private final String uuid;

    @NonNull
    private final String hardwareAddress;

    @Deprecated
    private final int major;

    @Deprecated
    private final int minor;

    @NonNull
    private final List beaconIdentifiers;

    private final int rssi;

    public Beacon(@NonNull String uuid,
           @NonNull String hardwareAddress,
           int major,
           int minor,
           int rssi,
           @NonNull List beaconIdentifiers) {
        this.uuid = uuid;
        this.hardwareAddress = hardwareAddress;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.beaconIdentifiers = beaconIdentifiers;
    }

    @Nullable
    @Override
    public UUID getIdentifierAsUuid(int index) {
        return ((UUID) beaconIdentifiers.get(index));
    }

    @Override
    public int getIdentifierAsInt(int index) {
        return ((Integer) beaconIdentifiers.get(index));
    }

    public byte getIdentifierAsByte(int index) {
        return (byte) beaconIdentifiers.get(index);
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    @NonNull
    public String getHardwareAddress() {
        return hardwareAddress;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRssi() {
        return rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Beacon beacon = (Beacon) o;
        return major == beacon.major
            && minor == beacon.minor
            && uuid.equals(beacon.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, major, minor);
    }

    @NonNull
    @Override
    public String toString() {
        return "Beacon{"
            + "uuid='" + uuid + '\''
            + ", hardwareAddress='" + hardwareAddress + '\''
            + ", major=" + major
            + ", minor=" + minor
            + ", rssi=" + rssi
            + '}';
    }
}
