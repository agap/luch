package aga.android.luch.distance;

import java.util.List;

import androidx.annotation.NonNull;

public interface IRssiFilter {

    byte getFilteredValue(@NonNull List<Reading> readings);
}
