package aga.android.ibeacon.demo

import aga.android.ibeacon.RegionDefinition
import android.content.Context

object RegionsDefinitionSource {

    fun getDefinitions(context: Context): List<RegionDefinition> = context
        .resources
        .getStringArray(R.array.uuids)
        .map {
            RegionDefinition(it)
        }
}