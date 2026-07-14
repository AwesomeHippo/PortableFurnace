package com.awesomehippo.portablefurnace.furnace;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.MathHelper;

public final class PortableFurnaceLogic {

    public static void tick(PortableFurnaceInventory furnace) {
        boolean dirty = false;

        furnace.beginBatch();
        try {
            if (furnace.isBurning()) {
                furnace.setBurnTime(furnace.getBurnTime() - 1);
                dirty = true;
            }

            ItemStack input = furnace.getInput();
            ItemStack fuel = furnace.getFuel();
            boolean hasInput = !input.isEmpty();
            boolean hasFuel = !fuel.isEmpty();

            if (furnace.isBurning() || (hasFuel && hasInput)) {
                ItemStack result = hasInput ? FurnaceRecipes.instance().getSmeltingResult(input) : ItemStack.EMPTY;
                boolean canSmelt = hasInput && !result.isEmpty() && canFitInOutput(furnace, result);

                if (!furnace.isBurning() && canSmelt && isFuel(fuel)) {
                    int burn = getBurnTime(fuel);
                    furnace.setBurnTime(burn);
                    furnace.setCurrentItemBurnTime(burn);

                    if (furnace.isBurning()) {
                        dirty = true;
                        consumeFuel(furnace, fuel);
                    }
                }

                if (furnace.isBurning() && canSmelt) {
                    furnace.setCookTime(furnace.getCookTime() + 1);

                    if (furnace.getCookTime() >= furnace.getCookTimeTotal()) {
                        furnace.setCookTime(0);
                        furnace.setCookTimeTotal(PortableFurnaceInventory.DEFAULT_COOK_TIME);
                        smeltItem(furnace, result);
                    }
                    dirty = true;
                } else if (furnace.getCookTime() != 0) {
                    furnace.setCookTime(0);
                    dirty = true;
                }
            } else if (!furnace.isBurning() && furnace.getCookTime() > 0) {
                int decayed = MathHelper.clamp(furnace.getCookTime() - 2, 0, furnace.getCookTimeTotal());
                if (decayed != furnace.getCookTime()) {
                    furnace.setCookTime(decayed);
                    dirty = true;
                }
            }

            if (dirty) {
                furnace.writeToHost();
            }
        } finally {
            furnace.endBatch();
        }
    }

    public static void tickStack(ItemStack stack) {
        if (!PortableFurnaceInventory.needsTick(stack)) {
            return;
        }
        tick(new PortableFurnaceInventory(stack));
    }

    public static boolean isFuel(ItemStack stack) {
        return !stack.isEmpty() && getBurnTime(stack) > 0;
    }

    private static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return TileEntityFurnace.getItemBurnTime(stack);
    }

    public static boolean isSmeltable(ItemStack stack) {
        return !stack.isEmpty() && !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
    }

    private static boolean canFitInOutput(PortableFurnaceInventory furnace, ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }

        ItemStack output = furnace.getOutput();
        if (output.isEmpty()) {
            return true;
        }
        if (!output.isItemEqual(result)) {
            return false;
        }

        int combined = output.getCount() + result.getCount();
        return combined <= furnace.getSlotLimit(PortableFurnaceInventory.SLOT_OUTPUT) && combined <= output.getMaxStackSize();
    }

    private static void consumeFuel(PortableFurnaceInventory furnace, ItemStack fuel) {
        if (fuel.isEmpty()) {
            return;
        }

        Item fuelItem = fuel.getItem();
        fuel.shrink(1);

        if (fuel.isEmpty()) {
            ItemStack container = fuelItem.getContainerItem(fuel);
            furnace.setStackInSlot(PortableFurnaceInventory.SLOT_FUEL, container.isEmpty() ? ItemStack.EMPTY : container);
        }
    }

    private static void smeltItem(PortableFurnaceInventory furnace, ItemStack result) {
        ItemStack input = furnace.getInput();
        ItemStack output = furnace.getOutput();

        if (output.isEmpty()) {
            furnace.setStackInSlot(PortableFurnaceInventory.SLOT_OUTPUT, result.copy());
        } else if (output.isItemEqual(result)) {
            output.grow(result.getCount());
            furnace.setStackInSlot(PortableFurnaceInventory.SLOT_OUTPUT, output);
        }

        // special case
        if (input.getItem() == Item.getItemFromBlock(Blocks.SPONGE) && input.getMetadata() == 1 && !furnace.getFuel().isEmpty() && furnace.getFuel().getItem() == Items.BUCKET) {
            furnace.setStackInSlot(PortableFurnaceInventory.SLOT_FUEL, new ItemStack(Items.WATER_BUCKET));
        }

        input.shrink(1);
        furnace.setStackInSlot(PortableFurnaceInventory.SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
    }
}
