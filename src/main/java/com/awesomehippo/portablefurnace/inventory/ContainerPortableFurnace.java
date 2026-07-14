package com.awesomehippo.portablefurnace.inventory;

import com.awesomehippo.portablefurnace.furnace.PortableFurnaceInventory;
import com.awesomehippo.portablefurnace.furnace.PortableFurnaceLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerPortableFurnace extends Container {

    private static final int PLAYER_INV_START = 3;
    private static final int PLAYER_INV_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;

    private final EntityPlayer player;
    private final EnumHand hand;
    private final PortableFurnaceInventory furnaceInventory;
    private final int lockedPlayerSlot;

    private int cookTime;
    private int cookTimeTotal;
    private int burnTime;
    private int currentItemBurnTime;

    public ContainerPortableFurnace(EntityPlayer player, EnumHand hand) {
        this.player = player;
        this.hand = hand;

        ItemStack host = player.getHeldItem(hand);
        this.furnaceInventory = new PortableFurnaceInventory(host);
        this.lockedPlayerSlot = hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : -1;

        this.addSlotToContainer(new SlotFurnaceInput(furnaceInventory, PortableFurnaceInventory.SLOT_INPUT, 56, 17));
        this.addSlotToContainer(new SlotFurnaceFuel(furnaceInventory, PortableFurnaceInventory.SLOT_FUEL, 56, 53));
        this.addSlotToContainer(new SlotFurnaceOutput(player, furnaceInventory, PortableFurnaceInventory.SLOT_OUTPUT, 116, 35));

        InventoryPlayer playerInv = player.inventory;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int index = col + row * 9 + 9;
                this.addSlotToContainer(new SlotLockedItem(playerInv, index, 8 + col * 18, 84 + row * 18, index == lockedPlayerSlot));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlotToContainer(new SlotLockedItem(playerInv, col, 8 + col * 18, 142, col == lockedPlayerSlot));
        }

        this.cookTime = furnaceInventory.getCookTime();
        this.cookTimeTotal = furnaceInventory.getCookTimeTotal();
        this.burnTime = furnaceInventory.getBurnTime();
        this.currentItemBurnTime = furnaceInventory.getCurrentItemBurnTime();
    }

    public boolean isTicking(ItemStack stack) {
        return furnaceInventory.isBoundTo(stack);
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getCookTimeTotal() {
        return cookTimeTotal;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getCurrentItemBurnTime() {
        return currentItemBurnTime;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, furnaceInventory.getCookTime());
        listener.sendWindowProperty(this, 1, furnaceInventory.getCookTimeTotal());
        listener.sendWindowProperty(this, 2, furnaceInventory.getBurnTime());
        listener.sendWindowProperty(this, 3, furnaceInventory.getCurrentItemBurnTime());
    }

    @Override
    public void detectAndSendChanges() {
        if (!player.world.isRemote && furnaceInventory.isBoundTo(player.getHeldItem(hand))) {
            PortableFurnaceLogic.tick(furnaceInventory);
        }

        super.detectAndSendChanges();

        for (IContainerListener listener : listeners) {
            if (cookTime != furnaceInventory.getCookTime()) {
                listener.sendWindowProperty(this, 0, furnaceInventory.getCookTime());
            }
            if (cookTimeTotal != furnaceInventory.getCookTimeTotal()) {
                listener.sendWindowProperty(this, 1, furnaceInventory.getCookTimeTotal());
            }
            if (burnTime != furnaceInventory.getBurnTime()) {
                listener.sendWindowProperty(this, 2, furnaceInventory.getBurnTime());
            }
            if (currentItemBurnTime != furnaceInventory.getCurrentItemBurnTime()) {
                listener.sendWindowProperty(this, 3, furnaceInventory.getCurrentItemBurnTime());
            }
        }

        cookTime = furnaceInventory.getCookTime();
        cookTimeTotal = furnaceInventory.getCookTimeTotal();
        burnTime = furnaceInventory.getBurnTime();
        currentItemBurnTime = furnaceInventory.getCurrentItemBurnTime();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0:
                cookTime = data;
                furnaceInventory.setCookTime(data);
                break;
            case 1:
                cookTimeTotal = data;
                furnaceInventory.setCookTimeTotal(data);
                break;
            case 2:
                burnTime = data;
                furnaceInventory.setBurnTime(data);
                break;
            case 3:
                currentItemBurnTime = data;
                furnaceInventory.setCurrentItemBurnTime(data);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn == this.player && furnaceInventory.isBoundTo(playerIn.getHeldItem(hand));
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
        if (clickType == ClickType.SWAP && lockedPlayerSlot >= 0 && dragType == lockedPlayerSlot) {
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickType, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getStack();
        original = stackInSlot.copy();

        final int input = PortableFurnaceInventory.SLOT_INPUT;
        final int fuel = PortableFurnaceInventory.SLOT_FUEL;
        final int output = PortableFurnaceInventory.SLOT_OUTPUT;

        if (index == output) {
            if (!this.mergeItemStack(stackInSlot, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onSlotChange(stackInSlot, original);
        } else if (index == input || index == fuel) {
            if (!this.mergeItemStack(stackInSlot, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INV_START) {
            if (PortableFurnaceLogic.isSmeltable(stackInSlot)) {
                if (!this.mergeItemStack(stackInSlot, input, input + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (PortableFurnaceLogic.isFuel(stackInSlot)) {
                if (!this.mergeItemStack(stackInSlot, fuel, fuel + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_INV_END) {
                if (!this.mergeItemStack(stackInSlot, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < HOTBAR_END) {
                if (!this.mergeItemStack(stackInSlot, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        if (stackInSlot.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(playerIn, stackInSlot);
        return original;
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        if (!playerIn.world.isRemote) {
            furnaceInventory.writeToHost();
        }
    }
}
