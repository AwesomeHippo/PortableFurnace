package com.awesomehippo.portablefurnace.inventory;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFurnaceOutput extends SlotItemHandler {

    private final EntityPlayer player;
    private int removeCount;

    public SlotFurnaceOutput(EntityPlayer player, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.player = player;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int amount) {
        if (getHasStack()) {
            removeCount += Math.min(amount, getStack().getCount());
        }
        return super.decrStackSize(amount);
    }

    @Override
    public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
        onCrafting(stack);
        super.onTake(thePlayer, stack);
        return stack;
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount) {
        removeCount += amount;
        onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack) {
        stack.onCrafting(player.world, player, removeCount);

        if (!player.world.isRemote) {
            // gives xp like vanilla furnaces (should be accurate)
            int amount = removeCount;
            float xpEach = FurnaceRecipes.instance().getSmeltingExperience(stack);

            if (xpEach == 0.0F) {
                amount = 0;
            } else if (xpEach < 1.0F) {
                int whole = MathHelper.floor(amount * xpEach);
                if (whole < MathHelper.ceil(amount * xpEach) && Math.random() < (double) (amount * xpEach - whole)) {
                    whole++;
                }
                amount = whole;
            }

            while (amount > 0) {
                int split = EntityXPOrb.getXPSplit(amount);
                amount -= split;
                player.world.spawnEntity(new EntityXPOrb(player.world, player.posX, player.posY + 0.5D, player.posZ + 0.5D, split));
            }

            FMLCommonHandler.instance().firePlayerSmeltedEvent(player, stack);
        }

        removeCount = 0;
    }
}
