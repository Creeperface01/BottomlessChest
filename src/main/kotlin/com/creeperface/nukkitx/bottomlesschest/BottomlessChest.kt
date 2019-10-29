package com.creeperface.nukkitx.bottomlesschest

import cn.nukkit.Player
import cn.nukkit.block.BlockChest
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.inventory.InventoryTransactionEvent
import cn.nukkit.event.player.PlayerInteractEvent
import cn.nukkit.event.player.PlayerQuitEvent
import cn.nukkit.inventory.transaction.action.SlotChangeAction
import cn.nukkit.plugin.PluginBase
import com.creeperface.nukkitx.bottomlesschest.blockentity.BlockEntityBottomlessChest
import com.creeperface.nukkitx.bottomlesschest.inventory.BottomlessInventory
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import cn.nukkit.utils.TextFormat as TF

/**
 * @author CreeperFace
 */
class BottomlessChest : PluginBase(), Listener {

    private val players = Long2ObjectOpenHashMap<Action>()

    override fun onLoad() {
        BlockEntity.registerBlockEntity("BottomlessChest", BlockEntityBottomlessChest::class.java)
    }

    override fun onEnable() {
        this.server.pluginManager.registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!command.name.equals("bchest", true)) {
            return false
        }

        if(sender !is Player) {
            return false
        }

        if (args.size != 1) {
            return false
        }

        if(!sender.hasPermission("bchest.use")) {
            return false
        }

        when (args[0]) {
            "create" -> {
                players[sender.id] = Action.CREATE
            }
            "remove" -> {
                players[sender.id] = Action.REMOVE
            }
            else -> {
                sender.sendMessage(TF.YELLOW.toString() + command.usage)
                return true
            }
        }

        sender.sendMessage("${TF.YELLOW}Now click on the target chest")
        return true
    }

    @EventHandler(ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        if(e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return
        }

        val p = e.player
        val action = players.remove(p.id) ?: return
        val b = e.block as? BlockChest ?: return
        val be = b.level.getBlockEntity(b) as? BlockEntityChest ?: return

        val chunk = b.level.getChunk(b.floorX shr 4, b.floorZ shr 4)
        chunk.setChanged()

        if(action == Action.CREATE) {
            be.close()
            be.saveNBT()

            val nbt = BlockEntity.getDefaultCompound(b, "BottomlessChest")
            nbt.putList(be.namedTag.getList("Items"))

            BlockEntityBottomlessChest(chunk, nbt)
            p.sendMessage("${TF.GREEN}Successfully removed bottom of the chest")
            e.setCancelled()
        } else {
            if(be !is BlockEntityBottomlessChest) {
                p.sendMessage("${TF.RED}This chest isn't bottomless")
                return
            }

            be.close()
            be.saveNBT()

            val nbt = BlockEntity.getDefaultCompound(b, BlockEntity.CHEST)
            nbt.putList(be.namedTag.getList("Items"))

            BlockEntityChest(chunk, nbt)
            p.sendMessage("${TF.GREEN}Successfully added the chest bottom back")
            e.setCancelled()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onTransaction(e: InventoryTransactionEvent) {
        val t = e.transaction
        val inv = t.inventories.singleOrNull { it is BottomlessInventory } ?: return

        t.actions.forEach { action ->
            if(action !is SlotChangeAction || action.inventory !is BottomlessInventory) {
                return@forEach
            }

            val src = action.sourceItem
            val target = action.targetItem

            if(target.count > src.count || (src.id != target.id && target.id != 0)) {
                e.setCancelled()
            }
        }

        if(!e.isCancelled) {
            inv.sendContents(t.source)
        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        players.remove(e.player.id)
    }

    private enum class Action {
        CREATE,
        REMOVE
    }
}