package aga.android.luch.rssi;

import androidx.annotation.Nullable;

public abstract class RssiFilter {

    protected RssiFilter() {

    }

    public abstract void addReading(byte rssi);

    @Nullable
    public abstract Byte getFilteredValue();

    public abstract static class Builder {

        public abstract RssiFilter build();
    }
}
