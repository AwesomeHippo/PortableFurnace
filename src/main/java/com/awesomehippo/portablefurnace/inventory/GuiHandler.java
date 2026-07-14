package com.awesomehippo.portablefurnace.inventory;

import com.awesomehippo.portablefurnace.PortableFurnace;
import com.awesomehippo.portablefurnace.client.GuiPortableFurnace;
import com.awesomehippo.portablefurnace.item.ItemPortableFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != PortableFurnace.GUI_PORTABLE_FURNACE) {
            return null;
        }

        EnumHand hand = handFromId(x);
        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemPortableFurnace)) {
            return null;
        }

        return new ContainerPortableFurnace(player, hand);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != PortableFurnace.GUI_PORTABLE_FURNACE) {
            return null;
        }

        EnumHand hand = handFromId(x);
        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemPortableFurnace)) {
            return null;
        }

        return new GuiPortableFurnace(player, hand);
    }

    private static EnumHand handFromId(int id) {
        return id == 1 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
    }
}
