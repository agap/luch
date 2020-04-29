package aga.android.luch;

import android.util.Log;

import androidx.annotation.NonNull;

public final class BeaconLogger {

    @SuppressWarnings("WeakerAccess")
    public static final Logger EMPTY_INSTANCE = new EmptyLogger();
    public static final Logger SYSTEM_INSTANCE = new SystemLogger();

    private static Logger LOGGER = EMPTY_INSTANCE;

    private BeaconLogger() {

    }

    static void d(@NonNull String message) {
        LOGGER.d(message);
    }

    static void e(@NonNull String message) {
        LOGGER.e(message);
    }

    public static void setInstance(@NonNull Logger logger) {
        LOGGER = logger;
    }

    @SuppressWarnings("WeakerAccess")
    public static abstract class Logger {

        abstract void d(@NonNull String message);

        abstract void e(@NonNull String message);
    }

    private static class EmptyLogger extends Logger {

        private EmptyLogger() {

        }

        @Override
        public void d(@NonNull String message) {
            // nothing to do here, I'm a dummy logger
        }

        @Override
        void e(@NonNull String message) {
            // nothing to do here as well
        }
    }

    private static class SystemLogger extends Logger {

        private SystemLogger() {

        }

        @Override
        public void d(@NonNull String message) {
            Log.d("BeaconLogging", message);
        }

        @Override
        void e(@NonNull String message) {
            Log.e("BeaconLogging", message);
        }
    }
}
