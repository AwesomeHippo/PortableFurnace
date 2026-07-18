package com.awesomehippo.portablefurnace.item;

import com.awesomehippo.portablefurnace.furnace.PortableFurnaceInventory;
import com.awesomehippo.portablefurnace.furnace.PortableFurnaceLogic;
import com.awesomehippo.portablefurnace.inventory.ContainerPortableFurnace;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPortableFurnace extends Item {

    public ItemPortableFurnace() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, inv, p) -> new ContainerPortableFurnace(id, inv, hand),
                    Component.translatable("container.portablefurnace.portable_furnace")
            ), buf -> buf.writeEnum(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (level.isClientSide || stack.isEmpty()) {
            return;
        }

        // container already ticks this stack while gui is open..
        if (entity instanceof Player player) {
            if (player.containerMenu instanceof ContainerPortableFurnace && ((ContainerPortableFurnace) player.containerMenu).isTicking(stack)) {
                return;
            }
        }

        PortableFurnaceLogic.tickStack(stack, level);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        // nbt flips every burn tick and the arm swing is annoying
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.isEmpty() || !stack.hasTag()) {
            tooltip.add(Component.literal("Right-click to open").withStyle(ChatFormatting.GRAY));
            return;
        }

        if (PortableFurnaceInventory.isBurning(stack)) {
            tooltip.add(Component.literal("Smelting...").withStyle(ChatFormatting.GOLD));
        }

        PortableFurnaceInventory inv = new PortableFurnaceInventory(stack);
        int filled = 0;
        for (int i = 0; i < PortableFurnaceInventory.SLOT_COUNT; i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                filled++;
            }
        }
        if (filled > 0) {
            tooltip.add(Component.literal("Items: " + filled + "/" + PortableFurnaceInventory.SLOT_COUNT).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.literal("Right-click to open").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return PortableFurnaceInventory.isBurning(stack);
    }
}
