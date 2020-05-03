package aga.android.luch;

import org.junit.Test;

public class ScanDurationTest {

    @Test(expected = AssertionError.class)
    public void testNegativeScanDurationIsProhibited() {

        // when
        final ScanDuration duration = ScanDuration.preciseDuration(-1, 100L);
    }

    @Test(expected = AssertionError.class)
    public void testNegativeRestDurationIsProhibited() {

        // when
        final ScanDuration duration = ScanDuration.preciseDuration(100L, -1);
    }

    @Test
    public void testNonNegativeScanAndRestValuesAreAccepted() {

        // when
        final ScanDuration duration = ScanDuration.preciseDuration(0L, 0L);
    }
}
