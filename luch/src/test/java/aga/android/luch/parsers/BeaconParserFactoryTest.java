package aga.android.luch.parsers;

import org.junit.Test;

import static aga.android.luch.parsers.BeaconParserFactory.createFromLayout;

public class BeaconParserFactoryTest {

    @Test(expected = IllegalArgumentException.class)
    public void testBeaconTypeFieldIsRequired() {

        // given
        final IBeaconParser parser = createFromLayout(
            "i:4-19,i:20-21,i:22-23,p:24-24,d:25-25",
            280
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBeaconTypeValueIsRequired() {

        // given
        final IBeaconParser parser = createFromLayout(
            "m:2-3,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25",
            280
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownFieldPrefixRaisesAnException() {

        // given
        final IBeaconParser parser = createFromLayout(
            "a:2-3,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25",
            280
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldsOfUnsupportedLengthRaisesAnException() {

        // given the last 'd' field has a length of 6
        final IBeaconParser parser = createFromLayout(
            "m:2-3,i:4-19,i:20-21,i:22-23,p:24-24,d:25-30",
            280
        );
    }
}
