package aga.android.ibeacon.demo

import aga.android.ibeacon.RegionDefinition
import android.content.Context

object RegionsDefinitionSource {

    fun getDefinitions(context: Context): List<RegionDefinition> {
//        context
//                .resources
//                .getStringArray(R.array.uuids)
//                .map {
//                    RegionDefinition(it)
//                }

        return listOf(RegionDefinition("e56e1f2c-c756-476f-8323-8d1f9cd245ea", 42819, 55646))
    }
}