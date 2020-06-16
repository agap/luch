package aga.android.luch.distance;

public class DistanceCalculatorFactory {

    public static AbstractDistanceCalculator getCalculator(int txPowerPosition) {

        return new DistanceCalculator(txPowerPosition);
    }
}
