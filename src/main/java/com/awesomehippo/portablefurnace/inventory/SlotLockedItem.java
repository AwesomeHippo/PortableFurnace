package com.awesomehippo.portablefurnace.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotLockedItem extends Slot {

    private final boolean locked;

    public SlotLockedItem(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean locked) {
        super(inventoryIn, index, xPosition, yPosition);
        this.locked = locked;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return !locked && super.canTakeStack(playerIn);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !locked && super.isItemValid(stack);
    }
}
