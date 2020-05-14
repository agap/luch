package aga.android.luch;

import java.util.UUID;

import androidx.annotation.Nullable;

public interface IBeacon {

    @Nullable
    UUID getIdentifierAsUuid(int index);

    int getIdentifierAsInt(int index);
}
