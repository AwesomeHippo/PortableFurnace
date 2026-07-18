package com.awesomehippo.portablefurnace.furnace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class PortableFurnaceInventory extends ItemStackHandler {

    private static final String NBT_ROOT = "PortableFurnace";
    private static final String NBT_ITEMS = "Items";
    private static final String NBT_BURN_TIME = "BurnTime";
    private static final String NBT_CURRENT_ITEM_BURN_TIME = "CurrentItemBurnTime";
    private static final String NBT_COOK_TIME = "CookTime";
    private static final String NBT_COOK_TIME_TOTAL = "CookTimeTotal";

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int DEFAULT_COOK_TIME = 200;

    private final ItemStack host;
    private int burnTime;
    private int currentItemBurnTime;
    private int cookTime;
    private int cookTimeTotal = DEFAULT_COOK_TIME;
    private boolean batch;

    public PortableFurnaceInventory(ItemStack host) {
        super(SLOT_COUNT);
        this.host = host;
        readFromHost();
    }

    public int getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = Math.max(0, burnTime);
    }

    public int getCurrentItemBurnTime() {
        return currentItemBurnTime;
    }

    public void setCurrentItemBurnTime(int currentItemBurnTime) {
        this.currentItemBurnTime = Math.max(0, currentItemBurnTime);
    }

    public int getCookTime() {
        return cookTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = Math.max(0, cookTime);
    }

    public int getCookTimeTotal() {
        return cookTimeTotal;
    }

    public void setCookTimeTotal(int cookTimeTotal) {
        this.cookTimeTotal = cookTimeTotal > 0 ? cookTimeTotal : DEFAULT_COOK_TIME;
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    public ItemStack getInput() {
        return getStackInSlot(SLOT_INPUT);
    }

    public ItemStack getFuel() {
        return getStackInSlot(SLOT_FUEL);
    }

    public ItemStack getOutput() {
        return getStackInSlot(SLOT_OUTPUT);
    }

    public boolean isBoundTo(ItemStack stack) {
        return this.host == stack;
    }

    public void beginBatch() {
        batch = true;
    }

    public void endBatch() {
        batch = false;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (!batch) {
            writeToHost();
        }
    }

    public void writeToHost() {
        if (host.isEmpty()) {
            return;
        }

        CompoundTag root = host.getOrCreateTag();
        CompoundTag data = new CompoundTag();

        data.put(NBT_ITEMS, serializeNBT());
        data.putInt(NBT_BURN_TIME, burnTime);
        data.putInt(NBT_CURRENT_ITEM_BURN_TIME, currentItemBurnTime);
        data.putInt(NBT_COOK_TIME, cookTime);
        data.putInt(NBT_COOK_TIME_TOTAL, cookTimeTotal);

        root.put(NBT_ROOT, data);
    }

    private void readFromHost() {
        burnTime = 0;
        currentItemBurnTime = 0;
        cookTime = 0;
        cookTimeTotal = DEFAULT_COOK_TIME;

        for (int i = 0; i < SLOT_COUNT; i++) {
            stacks.set(i, ItemStack.EMPTY);
        }

        if (host.isEmpty() || !host.hasTag()) {
            return;
        }

        CompoundTag root = host.getTag();
        if (!root.contains(NBT_ROOT, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag data = root.getCompound(NBT_ROOT);
        if (data.contains(NBT_ITEMS, Tag.TAG_COMPOUND)) {
            deserializeNBT(data.getCompound(NBT_ITEMS));
        }
        burnTime = Math.max(0, data.getInt(NBT_BURN_TIME));
        currentItemBurnTime = Math.max(0, data.getInt(NBT_CURRENT_ITEM_BURN_TIME));
        cookTime = Math.max(0, data.getInt(NBT_COOK_TIME));
        cookTimeTotal = data.contains(NBT_COOK_TIME_TOTAL) ? Math.max(1, data.getInt(NBT_COOK_TIME_TOTAL)) : DEFAULT_COOK_TIME;
    }

    // mini check to prevent idle furnaces deserializing every tick
    public static boolean needsTick(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }

        CompoundTag root = stack.getTag();
        if (!root.contains(NBT_ROOT, Tag.TAG_COMPOUND)) {
            return false;
        }

        CompoundTag data = root.getCompound(NBT_ROOT);
        if (data.getInt(NBT_BURN_TIME) > 0 || data.getInt(NBT_COOK_TIME) > 0) {
            return true;
        }

        if (!data.contains(NBT_ITEMS, Tag.TAG_COMPOUND)) {
            return false;
        }

        CompoundTag items = data.getCompound(NBT_ITEMS);
        if (!items.contains("Items", Tag.TAG_LIST)) {
            return false;
        }

        ListTag list = items.getList("Items", Tag.TAG_COMPOUND);
        boolean hasInput = false;
        boolean hasFuel = false;
        for (int i = 0; i < list.size(); i++) {
            int slot = list.getCompound(i).getByte("Slot") & 255;
            if (slot == SLOT_INPUT) {
                hasInput = true;
            } else if (slot == SLOT_FUEL) {
                hasFuel = true;
            }
            if (hasInput && hasFuel) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBurning(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        CompoundTag root = stack.getTag();
        return root.contains(NBT_ROOT, Tag.TAG_COMPOUND) && root.getCompound(NBT_ROOT).getInt(NBT_BURN_TIME) > 0;
    }
}
