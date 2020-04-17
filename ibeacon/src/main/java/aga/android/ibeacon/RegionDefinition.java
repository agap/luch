package aga.android.ibeacon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RegionDefinition {

    @NonNull
    private final String uuid;

    @Nullable
    private final Integer major;

    @Nullable
    private final Integer minor;

    public RegionDefinition(@NonNull String uuid, @Nullable Integer major, @Nullable Integer minor) {
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
