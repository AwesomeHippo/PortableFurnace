package com.awesomehippo.portablefurnace.inventory;

import com.awesomehippo.portablefurnace.ModMenus;
import com.awesomehippo.portablefurnace.furnace.PortableFurnaceInventory;
import com.awesomehippo.portablefurnace.furnace.PortableFurnaceLogic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

public class ContainerPortableFurnace extends AbstractContainerMenu {

    private static final int PLAYER_INV_START = 3;
    private static final int PLAYER_INV_END = 30;
    private static final int HOTBAR_START = 30;
    private static final int HOTBAR_END = 39;

    private final Player player;
    private final InteractionHand hand;
    private final PortableFurnaceInventory furnaceInventory;
    private final int lockedPlayerSlot;

    private int cookTime;
    private int cookTimeTotal;
    private int burnTime;
    private int currentItemBurnTime;

    public static ContainerPortableFurnace fromNetwork(int id, Inventory playerInv, FriendlyByteBuf buf) {
        return new ContainerPortableFurnace(id, playerInv, buf.readEnum(InteractionHand.class));
    }

    public ContainerPortableFurnace(int id, Inventory playerInv, InteractionHand hand) {
        super(ModMenus.PORTABLE_FURNACE.get(), id);
        this.player = playerInv.player;
        this.hand = hand;

        ItemStack host = player.getItemInHand(hand);
        this.furnaceInventory = new PortableFurnaceInventory(host);
        this.lockedPlayerSlot = hand == InteractionHand.MAIN_HAND ? playerInv.selected : -1;

        this.addSlot(new SlotFurnaceInput(player, furnaceInventory, PortableFurnaceInventory.SLOT_INPUT, 56, 17));
        this.addSlot(new SlotFurnaceFuel(furnaceInventory, PortableFurnaceInventory.SLOT_FUEL, 56, 53));
        this.addSlot(new SlotFurnaceOutput(player, furnaceInventory, PortableFurnaceInventory.SLOT_OUTPUT, 116, 35));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int index = col + row * 9 + 9;
                this.addSlot(new SlotLockedItem(playerInv, index, 8 + col * 18, 84 + row * 18, index == lockedPlayerSlot));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new SlotLockedItem(playerInv, col, 8 + col * 18, 142, col == lockedPlayerSlot));
        }

        this.cookTime = furnaceInventory.getCookTime();
        this.cookTimeTotal = furnaceInventory.getCookTimeTotal();
        this.burnTime = furnaceInventory.getBurnTime();
        this.currentItemBurnTime = furnaceInventory.getCurrentItemBurnTime();

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return furnaceInventory.getCookTime();
            }

            @Override
            public void set(int value) {
                cookTime = value;
                furnaceInventory.setCookTime(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return furnaceInventory.getCookTimeTotal();
            }

            @Override
            public void set(int value) {
                cookTimeTotal = value;
                furnaceInventory.setCookTimeTotal(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return furnaceInventory.getBurnTime();
            }

            @Override
            public void set(int value) {
                burnTime = value;
                furnaceInventory.setBurnTime(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return furnaceInventory.getCurrentItemBurnTime();
            }

            @Override
            public void set(int value) {
                currentItemBurnTime = value;
                furnaceInventory.setCurrentItemBurnTime(value);
            }
        });
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
    public void broadcastChanges() {
        if (!player.level().isClientSide && furnaceInventory.isBoundTo(player.getItemInHand(hand))) {
            PortableFurnaceLogic.tick(furnaceInventory, player.level());
        }

        super.broadcastChanges();

        cookTime = furnaceInventory.getCookTime();
        cookTimeTotal = furnaceInventory.getCookTimeTotal();
        burnTime = furnaceInventory.getBurnTime();
        currentItemBurnTime = furnaceInventory.getCurrentItemBurnTime();
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return playerIn == this.player && furnaceInventory.isBoundTo(playerIn.getItemInHand(hand));
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (clickType == ClickType.SWAP && lockedPlayerSlot >= 0 && dragType == lockedPlayerSlot) {
            return;
        }
        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        original = stackInSlot.copy();

        final int input = PortableFurnaceInventory.SLOT_INPUT;
        final int fuel = PortableFurnaceInventory.SLOT_FUEL;
        final int output = PortableFurnaceInventory.SLOT_OUTPUT;

        if (index == output) {
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stackInSlot, original);
        } else if (index == input || index == fuel) {
            if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INV_START) {
            if (PortableFurnaceLogic.isSmeltable(playerIn.level(), stackInSlot)) {
                if (!this.moveItemStackTo(stackInSlot, input, input + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (PortableFurnaceLogic.isFuel(stackInSlot)) {
                if (!this.moveItemStackTo(stackInSlot, fuel, fuel + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_INV_END) {
                if (!this.moveItemStackTo(stackInSlot, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < HOTBAR_END) {
                if (!this.moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(playerIn, stackInSlot);
        return original;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        if (!playerIn.level().isClientSide) {
            furnaceInventory.writeToHost();
        }
    }
}
