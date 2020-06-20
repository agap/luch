package aga.android.luch;

import android.os.SystemClock;

public interface ITimeProvider {

    long elapsedRealTimeTimeMillis();

    class SystemTimeProvider implements ITimeProvider {

        @Override
        public long elapsedRealTimeTimeMillis() {
            return SystemClock.elapsedRealtime();
        }
    }

    class TestTimeProvider implements ITimeProvider {

        public long elapsedRealTimeMillis = 0L;

        @Override
        public long elapsedRealTimeTimeMillis() {
            return elapsedRealTimeMillis;
        }
    }
}
