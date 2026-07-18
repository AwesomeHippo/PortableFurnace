package com.awesomehippo.portablefurnace.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

public class SlotLockedItem extends Slot {

    private final boolean locked;

    public SlotLockedItem(Container inventoryIn, int index, int xPosition, int yPosition, boolean locked) {
        super(inventoryIn, index, xPosition, yPosition);
        this.locked = locked;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !locked && super.mayPickup(playerIn);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !locked && super.mayPlace(stack);
    }
}
