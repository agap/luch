package aga.android.luch;

import java.util.Objects;

import androidx.annotation.NonNull;

public class Beacon {

    @NonNull
    private final String uuid;

    @NonNull
    private final String hardwareAddress;

    private final int major;

    private final int minor;

    private final int rssi;

    Beacon(@NonNull String uuid,
           @NonNull String hardwareAddress,
           int major,
           int minor,
           int rssi) {
        this.uuid = uuid;
        this.hardwareAddress = hardwareAddress;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
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
