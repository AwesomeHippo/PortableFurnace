package com.awesomehippo.portablefurnace.inventory;

import com.awesomehippo.portablefurnace.furnace.PortableFurnaceLogic;
import com.awesomehippo.portablefurnace.item.ItemPortableFurnace;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFurnaceInput extends SlotItemHandler {

    private final Player player;

    public SlotFurnaceInput(Player player, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.player = player;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof ItemPortableFurnace) {
            return false;
        }
        return PortableFurnaceLogic.isSmeltable(player.level(), stack);
    }
}
