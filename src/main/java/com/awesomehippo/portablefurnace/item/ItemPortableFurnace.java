package com.awesomehippo.portablefurnace.item;

import com.awesomehippo.portablefurnace.PortableFurnace;
import com.awesomehippo.portablefurnace.furnace.PortableFurnaceInventory;
import com.awesomehippo.portablefurnace.furnace.PortableFurnaceLogic;
import com.awesomehippo.portablefurnace.inventory.ContainerPortableFurnace;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPortableFurnace extends Item {

    public ItemPortableFurnace() {
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote) {
            int handId = hand == EnumHand.MAIN_HAND ? 0 : 1;
            player.openGui(PortableFurnace.instance, PortableFurnace.GUI_PORTABLE_FURNACE, world, handId, 0, 0);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote || stack.isEmpty()) {
            return;
        }

        // container already ticks this stack while gui is open..
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.openContainer instanceof ContainerPortableFurnace && ((ContainerPortableFurnace) player.openContainer).isTicking(stack)) {
                return;
            }
        }

        PortableFurnaceLogic.tickStack(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // nbt flips every burn tick and the arm swing is annoying
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            tooltip.add(TextFormatting.GRAY + "Right-click to open");
            return;
        }

        if (PortableFurnaceInventory.isBurning(stack)) {
            tooltip.add(TextFormatting.GOLD + "Smelting...");
        }

        PortableFurnaceInventory inv = new PortableFurnaceInventory(stack);
        int filled = 0;
        for (int i = 0; i < PortableFurnaceInventory.SLOT_COUNT; i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                filled++;
            }
        }
        if (filled > 0) {
            tooltip.add(TextFormatting.DARK_GRAY + "Items: " + filled + "/" + PortableFurnaceInventory.SLOT_COUNT);
        } else {
            tooltip.add(TextFormatting.GRAY + "Right-click to open");
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return PortableFurnaceInventory.isBurning(stack);
    }
}
