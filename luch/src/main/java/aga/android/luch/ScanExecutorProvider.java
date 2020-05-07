package aga.android.luch;

import java.util.concurrent.ScheduledExecutorService;

interface ScanExecutorProvider {

    ScheduledExecutorService provide();
}
