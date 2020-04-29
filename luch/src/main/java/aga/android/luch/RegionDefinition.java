package aga.android.luch;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RegionDefinition {

    @NonNull
    private final String uuid;

    @Nullable
    private final Integer major;

    @Nullable
    private final Integer minor;

    public RegionDefinition(@NonNull String uuid,
                            @Nullable @IntRange(from = 0, to = 65535) Integer major,
                            @Nullable @IntRange(from = 0, to = 65535) Integer minor) {

        if (major != null && (major < 0 || major > 65535)) {
            throw new AssertionError("Major has to be in range [0, 65535]; " +
                    "actual value is: " + major);
        }

        if (minor != null && (minor < 0 || minor > 65535)) {
            throw new AssertionError("Minor has to be in range [0, 65535]; " +
                    "actual value is: " + minor);
        }

        if ((major != null && minor == null) || (major == null && minor != null)) {
            throw new AssertionError("Both major and minor are expected to be either " +
                    "non-null or nullable at the same time; actual major is: " + major +
                    ", actual minor is: " + minor);
        }

        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public RegionDefinition(String uuid) {
        this(uuid, null, null);
    }

    @NonNull
    String getUuid() {
        return uuid;
    }

    @Nullable
    Integer getMajor() {
        return major;
    }

    @Nullable
    Integer getMinor() {
        return minor;
    }
}
