package aga.android.luch;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Beacon {

    @NonNull
    private final String hardwareAddress;

    @NonNull
    private final List beaconIdentifiers;

    public Beacon(@NonNull String hardwareAddress,
                  @NonNull List beaconIdentifiers) {
        this.hardwareAddress = hardwareAddress;
        this.beaconIdentifiers = beaconIdentifiers;
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

    @NonNull
    public String getHardwareAddress() {
        return hardwareAddress;
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
            + ", beaconIdentifiers=" + beaconIdentifiers
            + '}';
    }
}
