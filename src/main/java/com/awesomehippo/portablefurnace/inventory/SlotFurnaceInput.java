package com.awesomehippo.portablefurnace.inventory;

import com.awesomehippo.portablefurnace.furnace.PortableFurnaceLogic;
import com.awesomehippo.portablefurnace.item.ItemPortableFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFurnaceInput extends SlotItemHandler {

    public SlotFurnaceInput(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemPortableFurnace) {
            return false;
        }
        return PortableFurnaceLogic.isSmeltable(stack);
    }
}
