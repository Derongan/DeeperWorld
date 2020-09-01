package com.derongan.minecraft.deeperworld

import com.derongan.minecraft.deeperworld.services.canMoveSections
import com.derongan.minecraft.deeperworld.services.WorldManager
import com.mineinabyss.idofront.commands.CommandHolder
import com.mineinabyss.idofront.commands.execution.ExperimentalCommandDSL
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor
import com.mineinabyss.idofront.commands.extensions.actions.playerAction
import com.mineinabyss.idofront.messaging.info
import com.mineinabyss.idofront.messaging.success

@ExperimentalCommandDSL
object DeeperCommandExecutor : IdofrontCommandExecutor() {
    override val commands: CommandHolder = commands(deeperWorld) {
        "sectionoff" {
            playerAction {
                player.canMoveSections = false
                sender.success("Automatic TP disabled for ${player.name}")
            }
        }
        "sectionon" {
            playerAction {
                player.canMoveSections = true
                sender.success("Automatic TP enabled for ${player.name}")
            }
        }
        "linfo" {
            playerAction {
                val section = WorldManager.getSectionFor(player.location)
                if (section == null)
                    sender.info("${player.name} is not in a managed section")
                else
                    sender.info("${player.name} is in section ${section.key}")
            }
        }
    }
}