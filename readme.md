![Testing Flow](https://github.com/agap/luch/workflows/Testing%20Flow/badge.svg)

# Luch

A somewhat simplistic library built with only one purpose in mind - monitor nearby beacons when the app's in the foreground mode. The library is under development, check the demo (`sample` module) to see the example.  

Also, in case you're wondering - luch ("луч") means "beam" in Russian.

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

## Logs

To see the beacon logs in the logcat, replace the default implementation of the Logger with a system instance:

```kotlin
BeaconLogger.setInstance(BeaconLogger.SYSTEM_INSTANCE)
```

Don't forget to check that the app holds location permission, location services are on, and Bluetooth is enabled. The library will warn you by issuing a warning log statement, but it will not show any popups or anything of that sort.

## Supported beacons

By default, the library supports AltBeacon monitoring, but you can also set your own layout by writing something like that:

```kotlin
val beaconManufacturerId = 100 // you need to find the manufacturer id of your beacons, that's just an example
val beaconLayout = "<beacon-layout>" // search the Internet to find the layout string of your specific beacon

val beaconParser = BeaconParserFactory.createFromLayout(beaconLayout, beaconManufacturerId)

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

# FAQ

(Well, to be honest, no one has asked me these questions, but I decided to call it a section of Frequently Asked Questions nonetheless)

## What if I need Eddystone beacons?

The current BeaconParser is only capable of handling the AltBeacon beacons and certain proprietary beacon layouts. I might add Eddystone support in the future, but it might take quite some time.

## Background mode?

The way I see it, Google is constantly making sure we can do less and less in background mode (which is a good thing IMO, considering how many bad actors there're):

* Android 8 introduced background [location](https://developer.android.com/about/versions/oreo/background-location-limits) & [execution](https://developer.android.com/about/versions/oreo/background) limits.
* Android 10 introduced a "while in use" only location permission.
* In Android 11, there will be [no background location](https://developer.android.com/preview/privacy/location) permission option in the in-app permissions dialog.

So there will be no background mode support unless I decide it's possible to do something which would work reliably despite all these recent changes.

## What's the point of this library then?

1. I want something simple, without background mode, beacon caching, ranging, and all that stuff. The more code we put into production, the less stable the result becomes.
2. I need the APIs to look a bit different compared to altbeacon, all I want is to be notified periodically about which beacons are near me, without individual beacon enter/exit events.
3. Just have some fun. :)
