package com.awesomehippo.portablefurnace.inventory;

import com.awesomehippo.portablefurnace.furnace.PortableFurnaceLogic;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFurnaceOutput extends SlotItemHandler {

    private final Player player;
    private int removeCount;

    public SlotFurnaceOutput(Player player, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.player = player;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        if (hasItem()) {
            removeCount += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
        checkTakeAchievements(stack);
        super.onTake(thePlayer, stack);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        removeCount += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(player.level(), player, removeCount);

        if (!player.level().isClientSide) {
            // gives xp like vanilla furnaces (should be accurate)
            int amount = removeCount;
            float xpEach = PortableFurnaceLogic.getSmeltingExperience(player.level(), stack);

            if (xpEach == 0.0F) {
                amount = 0;
            } else if (xpEach < 1.0F) {
                int whole = Mth.floor(amount * xpEach);
                if (whole < Mth.ceil(amount * xpEach) && Math.random() < (double) (amount * xpEach - whole)) {
                    whole++;
                }
                amount = whole;
            }

            while (amount > 0) {
                int split = ExperienceOrb.getExperienceValue(amount);
                amount -= split;
                player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, split));
            }

            ForgeEventFactory.firePlayerSmeltedEvent(player, stack);
        }

        removeCount = 0;
    }
}
