package com.awesomehippo.portablefurnace.inventory;

import com.awesomehippo.portablefurnace.furnace.PortableFurnaceLogic;
import com.awesomehippo.portablefurnace.item.ItemPortableFurnace;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFurnaceFuel extends SlotItemHandler {

    public SlotFurnaceFuel(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemPortableFurnace) {
            return false;
        }
        return PortableFurnaceLogic.isFuel(stack) || stack.getItem() == Items.BUCKET;
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        return stack.getItem() == Items.BUCKET ? 1 : super.getItemStackLimit(stack);
    }
}
