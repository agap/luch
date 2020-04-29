![Testing Flow](https://github.com/agap/luch/workflows/Testing%20Flow/badge.svg)

# Luch

A somewhat simplistic library which aims to do one thing only - monitor nearby iBeacons when the app's in foreground mode. The library is under development, check the demo (`app` module) to see the example.

## What if I need Eddystone or other beacon formats?

There is nothing except for iBeacon now, if you need other formats then please check out [altbeacon](https://altbeacon.github.io/android-beacon-library/).

## Background mode?

Sorry, my experience tells me that unless Google adds native support of iBeacons in OS, all solutions we come up with will be half-baked, especially considering the recent changes related to background location access.

## What's the point of this library then?

1. I want something simple, without background mode, beacon caching, ranging and all that stuff. The more code we put into production, the less stable the result becomes.
2. I need the APIs to look a bit different compared to altbeacon. First, I want to be notified periodically about all beacons which are nearby, not the individual enter/exit events (having the individual enter/exit events means that we need to support the overall view of nearby beacons ourselves and I want to hide it). Second, I want to be able to scan for nearby beacons while filtering by proximity UUIDs only, and I expect to see the beacons' major/minor values in the responses. `altbeacon` hides the major/minor from the monitoring results unless you specified UUID **and** major **and** minor (I know they did it for compatibility with iOS, but still, I don't like that behaviour).
3. I'm a bit afraid of updating the `altbeacon` library in the projects where I use it, since I had a somewhat painful experience with it - sometimes I catch the performance regressions during the acceptance tests, sometimes I catch crashes during the staged rollout. Both happened a bit too often, so I _hope_ that this library might turn out to be more stable by being less complex (famous last words).
4. Just have some fun. :)
