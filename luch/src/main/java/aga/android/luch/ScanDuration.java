package aga.android.luch;

import java.util.concurrent.TimeUnit;

import androidx.annotation.IntRange;

public final class ScanDuration {

    public static final ScanDuration UNIFORM = ScanDuration.preciseDuration(
        TimeUnit.SECONDS.toMillis(6),
        TimeUnit.SECONDS.toMillis(6)
    );

    final long scanDurationMillis;
    final long restDurationMillis;

    private ScanDuration(long scanDurationMillis,
                         long restDurationMillis) {
        this.scanDurationMillis = scanDurationMillis;
        this.restDurationMillis = restDurationMillis;
    }

    /**
     * Creates a new {@link ScanDuration} object having the specified scan and rest durations.
     *
     * <b>Attention:</b> starting from Android 7.0, we're not allowed to start/stop the BLE
     * scans more often than 5 times in 30 seconds; if we do, OS will consider this behaviour
     * harmful and it will not deliver any {@link android.bluetooth.le.ScanResult} until the end
     * of that 30 second period. Since UNIFORM object has both the scan and rest duration equal
     * to 6 seconds, we end up having precisely 5 stops/starts within 30 second (almost, because
     * there is no such thing as preciseness in the real world) which is considered to be
     * non-harmful behaviour.
     *
     * Hence, use preciseDuration at your own risk.
     */
    public static ScanDuration preciseDuration(@IntRange(from = 0) long scanDurationMillis,
                                               @IntRange(from = 0) long restDurationMillis) {
        if (scanDurationMillis < 0) {
            throw new AssertionError("Scan duration has to be non-negative; actual value is: " +
                    scanDurationMillis);
        }

        if (restDurationMillis < 0) {
            throw new AssertionError("Rest duration has to be non-negative; actual value is: " +
                    restDurationMillis);
        }

        return new ScanDuration(scanDurationMillis, restDurationMillis);
    }
}
