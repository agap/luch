package aga.android.ibeacon;

import java.util.Objects;

import androidx.annotation.NonNull;

public class Beacon {

    @NonNull
    private final String uuid;

    private final int major;

    private final int minor;

    public Beacon(@NonNull String uuid, int major, int minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Beacon beacon = (Beacon) o;
        return major == beacon.major &&
                minor == beacon.minor &&
                uuid.equals(beacon.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, major, minor);
    }

    @Override
    public String toString() {
        return "Beacon{" +
                "uuid='" + uuid + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                '}';
    }
}
