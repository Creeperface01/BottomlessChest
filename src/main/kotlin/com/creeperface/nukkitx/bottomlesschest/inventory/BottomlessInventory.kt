package com.creeperface.nukkitx.bottomlesschest.inventory

import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.inventory.ChestInventory
import cn.nukkit.item.Item

/**
 * @author CreeperFace
 */
class BottomlessInventory(chest: BlockEntityChest, contents: Map<Int, Item>) : ChestInventory(chest) {

    private var allowChange = false

    init {
        setContentsForce(contents)
    }

    override fun clear(index: Int, send: Boolean): Boolean {
        if(allowChange) {
            return super.clear(index, send)
        }

        return true
    }

    override fun clearAll() {
        if(allowChange) {
            super.clearAll()
        }
    }

    override fun setItem(index: Int, item: Item?, send: Boolean): Boolean {
        if(allowChange) {
            return super.setItem(index, item, send)
        }

        return true
    }

    override fun setContents(items: Map<Int, Item>?) {

    }

    fun setContentsForce(items: Map<Int, Item>) {
        allowChange = true

        try {
            super.setContents(items)
        } finally {
            allowChange = false
        }
    }
}