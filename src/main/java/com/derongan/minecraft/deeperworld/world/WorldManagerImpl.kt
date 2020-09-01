package com.derongan.minecraft.deeperworld.world

import com.derongan.minecraft.deeperworld.services.WorldManager
import com.derongan.minecraft.deeperworld.world.section.AbstractSectionKey.CustomSectionKey
import com.derongan.minecraft.deeperworld.world.section.AbstractSectionKey.InternalSectionKey
import com.derongan.minecraft.deeperworld.world.section.Section
import com.derongan.minecraft.deeperworld.world.section.SectionKey
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import java.util.stream.Collectors

class WorldManagerImpl(config: FileConfiguration) : WorldManager {
    override val sections get() = sectionMap.values.toSet()

    private val sectionMap: MutableMap<SectionKey, Section> = HashMap()

    private fun getKey(sectionData: Map<*, *>): SectionKey {
        val name = sectionData[NAME_KEY] as String?
        return name?.let { CustomSectionKey(it) } ?: InternalSectionKey()
    }

    private fun parseLocation(points: List<Int>?, world: World?): Location {
        return Location(world, points!![0].toDouble(), points[1].toDouble(), points[2].toDouble())
    }

    override fun getSectionFor(location: Location): Section? {
        return getSectionFor(location.blockX, location.blockZ, location.world!!)
    }

    override fun registerSection(name: String, section: Section): SectionKey =
            registerInternal(CustomSectionKey(name), section)

    override fun registerSection(sectionKey: SectionKey, section: Section): SectionKey =
            registerInternal(sectionKey, section)

    private fun registerInternal(key: SectionKey, section: Section): SectionKey {
        if (sectionMap.containsKey(key)) throw RuntimeException("Bruh") //TODO change to checked exception
        sectionMap[key] = section
        return key
    }

    override fun unregisterSection(key: SectionKey) = TODO()

    override fun getSectionFor(x: Int, z: Int, world: World): Section? = //TODO consider performance
            sectionMap.values.firstOrNull { it.world == world && it.region.contains(x, z) }

    override fun getSectionFor(key: SectionKey) = sectionMap[key]
    override fun getSectionFor(key: String) = sectionMap[CustomSectionKey(key)]

    companion object {
        const val SECTION_KEY = "sections"
        const val REF_TOP_KEY = "refTop"
        const val REF_BOTTOM_KEY = "refBottom"
        const val WORLD_KEY = "world"
        const val REGION_KEY = "region"
        const val NAME_KEY = "name"
    }

    init {
        val sectionList = config.getMapList(SECTION_KEY)
        val keys = sectionList.stream().map { sectionData: Map<*, *> -> getKey(sectionData) }.collect(Collectors.toList())

        @Suppress("UNCHECKED_CAST")
        for (i in keys.indices) {
            val map = sectionList[i]
            val worldName = map[WORLD_KEY] as String
            val world = Bukkit.getWorld(worldName)!!
            val regionPoints = map[REGION_KEY] as List<Int>
            val region = Region(regionPoints[0], regionPoints[1], regionPoints[2], regionPoints[3]) //TODO use worldguard regions in the future
            val refBottom = parseLocation(map[REF_BOTTOM_KEY] as List<Int>, world)
            val refTop = parseLocation(map[REF_TOP_KEY] as List<Int>, world)

            val section = Section(
                    world = world,
                    region = region,
                    key = keys[i],
                    aboveKey = if (i != 0) keys[i - 1] else SectionKey.TERMINAL,
                    belowKey = if (i < keys.size - 1) keys[i + 1] else SectionKey.TERMINAL,
                    referenceTop = refTop,
                    referenceBottom = refBottom,
            )

            registerSection(keys[i], section)
        }
    }
}