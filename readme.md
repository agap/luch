![CI](https://github.com/agap/luch/workflows/CI/badge.svg) [![Maintainability](https://api.codeclimate.com/v1/badges/16b6d5e3528035e9d334/maintainability)](https://codeclimate.com/github/agap/luch/maintainability) [![codecov](https://codecov.io/gh/agap/luch/branch/master/graph/badge.svg)](https://codecov.io/gh/agap/luch) ![Bintray](https://img.shields.io/bintray/v/agap/maven/luch) ![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

# Luch

A somewhat simplistic library built with only one purpose in mind - monitor nearby beacons when the app's in the foreground mode. The library is under development, check the demo (`sample` module) to see the example.

Also, in case you're wondering - luch ("луч") means "beam" in Russian.

# Installation

Make sure your project's `build.gradle` defines access to JCenter Repository:

```groovy
buildscript {
    repositories {
        jcenter()
    }
}
```
Add the following dependency to your app module's `build.gradle` file:

```groovy
implementation 'aga.android:luch:(insert latest version)'
```

# Basic Usage

## Obtain an instance of the BeaconScanner

```kotlin
val beaconScanner = BeaconScanner.Builder()
    .setBeaconListener { beacons: Set<Beacon> ->
        // do something with your beacons here
    }
    .build()
```

## Subscribe when the app is in the foreground

```kotlin
beaconScanner.start()
```

## Unsubscribe when the app gets backgrounded

```kotlin
beaconScanner.stop()
```

That's it!

# Settings

## Regions

If you want to monitor specific beacons, you can provide a list of regions to look for when you're creating a BeaconScanner:

```kotlin
val regions = listOf(Region.Builder().build())

val beaconScanner = BeaconScanner.Builder()
    .setRegions(regions)
    .build()
```

Each `Region` is created via its `Builder` by specifying the fields you're interested in. For example, you want to monitor the AltBeacon beacon having a UUID of `"01234567-0123-4567-89AB-456789ABCDEF"`, you'd end up writing something like that:

```kotlin
val region = Region.Builder()
    .setNullField()
    .setUuidField(UUID.fromString("01234567-0123-4567-89AB-456789ABCDEF"))
    .build()
```

If you want to monitor the AltBeacon beacon that has a specific UUID, major and minor, you'd write something like that:

```kotlin
val region = Region.Builder()
    .setNullField()
    .setUuidField(UUID.fromString("01234567-0123-4567-89AB-456789ABCDEF"))
    .setIntegerField(154)
    .setIntegerField(10122)
    .build()
```

If you're curious about why you need to add a null field first, the reason is quite simple. An AltbBeacon's beacon layout looks like that:

`m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25`

The first field is a beacon type field that occupies the 2nd and 3rd bytes and has a value of `"BEAC"`. Then we have an `UUID` field (bytes 4-19), major field (bytes 20-21), minor field (bytes 22-23), and two additional single-byte fields. Since we don't want to filter by the beacon type, we ignore it by specifying the `null` value for that field.

## Beacons

You can access the `Beacon`'s fields in more or less similar fashion as the `Region`'s fields - once you get a `Beacon` object, you can inspect its data by calling getter methods:

```kotlin
val uuid  = beacon.getIdentifierAsUuid(1)
val major = beacon.getIdentifierAsInt(2)
```

The field index corresponds to the index from the beacon's layout (major has an index of 2, minor has an index of 3, etc.). This is done mostly because there're tons of different beacons and layouts, and it's easier to treat a Beacon as a sequence of generic fields instead of trying to give these fields specific names (identifiers, RSSI, data fields, etc.).

If you don't want to work with integer indices, you can create your own `Beacon` entity with the properly named field and map the Luch Beacons into your app's beacons as soon as you get notified via `IBeaconListener`. It also leads to a better separation between the library and your own code and makes it easier to replace one beacon scanning library with another in the future.

## Supported beacons

By default, the library supports AltBeacon monitoring, but you can also set your own layout by writing something like that:

```kotlin
val beaconLayout = "<beacon-layout>" // search the Internet to find the layout string of your specific beacon

val beaconParser = BeaconParserFactory.createFromLayout(beaconLayout)

val beaconScanner = BeaconScanner.Builder()
    .setBeaconParser(beaconParser)
    .setBeaconListener { beacons: Set<Beacon> ->
        // do something with your beacons here
    }
    .build()
```

The format of beacon layouts is somewhat similar to the one supported by [AltBeacon](https://altbeacon.github.io/android-beacon-library/javadoc/reference/org/altbeacon/beacon/BeaconParser.html) (see `setBeaconLayout` method) with the number of exceptions:

1. The only field prefixes supported at the moment are 'm', 'i', 'p' and 'd'.
2. Little-endian fields are not supported yet, as variable-length fields.

## Distance calculation

You can range your beacons if you want to. To do that, build your `BeaconScanner` with ranging support:

```kotlin
val beaconScanner = BeaconScanner.Builder()
    .setBeaconListener { beacons: Set<Beacon> ->
        // do something with your beacons here
    }
    .setRangingEnabled()
    .build()

beaconScanner.start()
```

Once you start beacon scans, you can access the scanner's `Ranger` object. This object does the distance calculation for a detected beacon:

```kotlin
val ranger = beaconScanner.getRanger()

val distance = ranger.calculateDistance(beacon)
```

The ranging works only for the beacons that provide the TxPower value in their advertisement packages (AltBeacon is one of them). Another component of distance calculation is RSSI value, which changes over time.
Due to the nature of BLE, RSSI values can suddenly change. To smooth these sudden changes, Luch uses the RSSI filtering technique. The default filter is [running average](https://en.wikipedia.org/wiki/Moving_average) filter, but you can replace it with [ARMA](https://en.wikipedia.org/wiki/Autoregressive%E2%80%93moving-average_model) (autoregressive–moving-average filter):

```kotlin
val beaconScanner = BeaconScanner.Builder()
    .setBeaconListener { beacons: Set<Beacon> ->
        // do something with your beacons here
    }
    .setRangingEnabled(
        ArmaFilter.Builder()
    )
    .build()

beaconScanner.start()
```

You can provide your own filters by extending the `RssiFilter` class.

## Logs

To see the beacon logs in the logcat, replace the default implementation of the Logger with a system instance:

```kotlin
BeaconLogger.setInstance(BeaconLogger.SYSTEM_INSTANCE)
```

Don't forget to check that the app holds location permission, location services are on, and Bluetooth is enabled. The library will warn you by issuing a warning log statement, but it will not show any popups or anything of that sort.

# FAQ

(Well, to be honest, no one has asked me these questions, but I decided to call it a section of Frequently Asked Questions nonetheless)

## What if I need Eddystone beacons?

The current `BeaconParser` is only capable of handling the AltBeacon beacons and certain proprietary beacon layouts. I might add Eddystone support in the future, but it might take quite some time.

## Background mode?

The way I see it, Google is constantly making sure we can do less and less in background mode (which is a good thing IMO, considering how many bad actors there're):

* Android 8 introduced background [location](https://developer.android.com/about/versions/oreo/background-location-limits) & [execution](https://developer.android.com/about/versions/oreo/background) limits.
* Android 10 introduced a "while in use" only location permission.
* In Android 11, there will be [no background location](https://developer.android.com/preview/privacy/location) permission option in the in-app permissions dialog.

So there will be no background mode support unless I decide it's possible to do something which would work reliably despite all these recent changes.

## What's the point of this library then?

1. I want something simple, without background mode, beacon caching, and all that stuff. The more code we put into production, the less stable the result becomes.
2. Just have some fun. :)
