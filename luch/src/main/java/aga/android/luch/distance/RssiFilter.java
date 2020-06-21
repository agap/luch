package aga.android.luch.distance;

import androidx.annotation.Nullable;

public abstract class RssiFilter {

    protected RssiFilter() {

    }

    abstract void addReading(byte rssi);

    @Nullable
    abstract Byte getFilteredValue();

    public abstract static class Builder {

        abstract RssiFilter build();
    }
}
