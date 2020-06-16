package aga.android.luch.demo.data

data class BeaconModel(
    val uuid: String,
    val major: Int,
    val minor: Int,
    val rssi: Int,
    val hardwareAddress: String,
    val distance: Double
)