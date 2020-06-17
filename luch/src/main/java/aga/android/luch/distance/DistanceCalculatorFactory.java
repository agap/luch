package aga.android.luch.distance;

public final class DistanceCalculatorFactory {

    private DistanceCalculatorFactory() {
        // no instances please
    }

    public static AbstractDistanceCalculator getCalculator(int txPowerPosition) {

        return new DistanceCalculator(txPowerPosition);
    }
}
