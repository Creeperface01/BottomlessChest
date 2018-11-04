package com.creeperface.nukkitx.bottomlesschest.blockentity

import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag
import com.creeperface.nukkitx.bottomlesschest.inventory.BottomlessInventory

/**
 * @author CreeperFace
 */
class BlockEntityBottomlessChest(chunk: FullChunk, nbt: CompoundTag) : BlockEntityChest(chunk, nbt) {

    override fun initBlockEntity() {
        super.initBlockEntity()

        this.inventory = BottomlessInventory(this, this.inventory.contents)
    }
}