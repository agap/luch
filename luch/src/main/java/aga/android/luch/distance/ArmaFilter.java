package aga.android.luch.distance;

import androidx.annotation.Nullable;

public class ArmaFilter extends RssiFilter {

    private final float armaSpeed;

    private Byte filteredRssi = null;

    private ArmaFilter(float armaSpeed) {
        this.armaSpeed = armaSpeed;
    }

    @Override
    void addReading(byte rssi) {
        if (filteredRssi == null) {
            filteredRssi = rssi;
        } else {
            filteredRssi = (byte) (filteredRssi - (armaSpeed * (filteredRssi - rssi)));
        }
    }

    @Nullable
    @Override
    Byte getFilteredValue() {
        return filteredRssi;
    }

    public static final class Builder extends RssiFilter.Builder {

        private static final float DEFAULT_ARMA_SPEED = 0.1f;

        private float armaSpeed = DEFAULT_ARMA_SPEED;

        public Builder() {

        }

        ArmaFilter.Builder setArmaSpeed(float armaSpeed) {
            this.armaSpeed = armaSpeed;
            return this;
        }

        @Override
        RssiFilter build() {
            return new ArmaFilter(armaSpeed);
        }
    }
}
