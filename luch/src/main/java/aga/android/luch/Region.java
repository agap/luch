package aga.android.luch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;

import static java.lang.String.format;

public class Region {

    private List regionFields = new ArrayList();

    private Region(@NonNull List regionFields) {
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

    @SuppressWarnings("WeakerAccess")
    public static class Builder {

        private List regionFields = new ArrayList();

        public Builder() {

        }

        public Builder addNullField() {
            //noinspection unchecked
            regionFields.add(null);
            return this;
        }

        public Builder addIntegerField(int field) {
            if (field < 0 || field > 65535) {
                throw new IllegalArgumentException(
                    format(
                        "Can't add a field %d, expected value is in range [0, 65535]",
                        field
                    )
                );
            }
            //noinspection unchecked
            regionFields.add(field);
            return this;
        }

        public Builder addUuidField(@NonNull UUID field) {
            //noinspection unchecked
            regionFields.add(field);
            return this;
        }

        public Builder addByteField(byte field) {
            //noinspection unchecked
            regionFields.add(field);
            return this;
        }

        public Region build() {
            return new Region(regionFields);
        }
    }
}
