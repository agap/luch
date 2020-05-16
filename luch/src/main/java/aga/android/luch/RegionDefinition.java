package aga.android.luch;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class RegionDefinition {

    private List regionFields = new ArrayList();

    public RegionDefinition(@NonNull List regionFields) {
        //noinspection unchecked
        this.regionFields.addAll(regionFields);

    }

    public Object getFieldAt(int i) {
        if (regionFields.size() > i) {
            return regionFields.get(i);
        } else {
            return null;
        }
    }
}
