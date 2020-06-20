package aga.android.luch.distance;

import org.junit.Test;

import java.util.Collections;

import aga.android.luch.Beacon;
import aga.android.luch.ITimeProvider.TestTimeProvider;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ReadingsCacheTest {

    private final TestTimeProvider timeProvider = new TestTimeProvider();

    @Test
    public void testAddReading() {
        // given
        final ReadingsCache cache = new ReadingsCache(timeProvider);
        final Beacon beacon = new Beacon("00:11:22:33:44:EE", emptyList(), (byte) -95);

        // when
        timeProvider.elapsedRealTimeMillis = 100;
        cache.add(beacon, (byte) -120);

        timeProvider.elapsedRealTimeMillis = 200;
        cache.add(beacon, (byte) -125);

        timeProvider.elapsedRealTimeMillis = 300;
        cache.add(beacon, (byte) -123);

        // then
        assertEquals(
            asList(
                new Reading((byte) -120, 100),
                new Reading((byte) -125, 200),
                new Reading((byte) -123, 300)
            ),
            cache.getReadingsOf(beacon)
        );
    }

    @Test
    public void testRemoveReadings() {
        // given
        final ReadingsCache cache = new ReadingsCache(timeProvider);
        final Beacon beacon = new Beacon("00:11:22:33:44:EE", emptyList(), (byte) -95);

        // when
        timeProvider.elapsedRealTimeMillis = 100;
        cache.add(beacon, (byte) -120);

        timeProvider.elapsedRealTimeMillis = 200;
        cache.add(beacon, (byte) -125);

        timeProvider.elapsedRealTimeMillis = 300;
        cache.add(beacon, (byte) -123);

        cache.remove(beacon);

        // then
        assertEquals(
            Collections.<Reading>emptyList(),
            cache.getReadingsOf(beacon)
        );
    }

    @Test
    public void testTrimReadings() {
        // given
        final ReadingsCache cache = new ReadingsCache(300, timeProvider);
        final Beacon beaconA = new Beacon("00:11:22:33:44:55", emptyList(), (byte) -95);
        final Beacon beaconB = new Beacon("00:11:22:33:44:66", emptyList(), (byte) -95);

        // when
        timeProvider.elapsedRealTimeMillis = 100;
        cache.add(beaconA, (byte) -120);
        cache.add(beaconB, (byte) -120);
        cache.add(beaconB, (byte) -118);

        timeProvider.elapsedRealTimeMillis = 250;
        cache.add(beaconA, (byte) -125);

        timeProvider.elapsedRealTimeMillis = 410;
        cache.add(beaconB, (byte) -123);

        cache.trim();

        // then
        assertEquals(
            singletonList(new Reading((byte) -125, 250)),
            cache.getReadingsOf(beaconA)
        );

        assertEquals(
            singletonList(new Reading((byte) -123, 410)),
            cache.getReadingsOf(beaconB)
        );
    }

}
