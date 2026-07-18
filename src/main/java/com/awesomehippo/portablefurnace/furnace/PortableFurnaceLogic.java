package com.awesomehippo.portablefurnace.furnace;

import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeHooks;

import java.util.Optional;

public final class PortableFurnaceLogic {

    public static void tick(PortableFurnaceInventory furnace, Level level) {
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
                ItemStack result = hasInput ? getSmeltingResult(level, input) : ItemStack.EMPTY;
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
                int decayed = Mth.clamp(furnace.getCookTime() - 2, 0, furnace.getCookTimeTotal());
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

    public static void tickStack(ItemStack stack, Level level) {
        if (!PortableFurnaceInventory.needsTick(stack)) {
            return;
        }
        tick(new PortableFurnaceInventory(stack), level);
    }

    public static boolean isFuel(ItemStack stack) {
        return !stack.isEmpty() && getBurnTime(stack) > 0;
    }

    private static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    public static boolean isSmeltable(Level level, ItemStack stack) {
        return !stack.isEmpty() && !getSmeltingResult(level, stack).isEmpty();
    }

    public static ItemStack getSmeltingResult(Level level, ItemStack input) {
        if (level == null || input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Optional<SmeltingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(input), level);
        return recipe.map(r -> r.getResultItem(level.registryAccess()).copy()).orElse(ItemStack.EMPTY);
    }

    public static float getSmeltingExperience(Level level, ItemStack result) {
        if (level == null || result.isEmpty()) {
            return 0.0F;
        }
        for (SmeltingRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING)) {
            if (ItemStack.isSameItem(recipe.getResultItem(level.registryAccess()), result)) {
                return recipe.getExperience();
            }
        }
        return 0.0F;
    }

    private static boolean canFitInOutput(PortableFurnaceInventory furnace, ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }

        ItemStack output = furnace.getOutput();
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItem(output, result)) {
            return false;
        }

        int combined = output.getCount() + result.getCount();
        return combined <= furnace.getSlotLimit(PortableFurnaceInventory.SLOT_OUTPUT) && combined <= output.getMaxStackSize();
    }

    private static void consumeFuel(PortableFurnaceInventory furnace, ItemStack fuel) {
        if (fuel.isEmpty()) {
            return;
        }

        ItemStack fuelCopy = fuel.copy();
        fuel.shrink(1);

        if (fuel.isEmpty()) {
            ItemStack container = fuelCopy.getCraftingRemainingItem();
            furnace.setStackInSlot(PortableFurnaceInventory.SLOT_FUEL, container.isEmpty() ? ItemStack.EMPTY : container);
        }
    }

    private static void smeltItem(PortableFurnaceInventory furnace, ItemStack result) {
        ItemStack input = furnace.getInput();
        ItemStack output = furnace.getOutput();

        if (output.isEmpty()) {
            furnace.setStackInSlot(PortableFurnaceInventory.SLOT_OUTPUT, result.copy());
        } else if (ItemStack.isSameItem(output, result)) {
            output.grow(result.getCount());
            furnace.setStackInSlot(PortableFurnaceInventory.SLOT_OUTPUT, output);
        }

        // special case
        if (input.is(Blocks.WET_SPONGE.asItem()) && !furnace.getFuel().isEmpty() && furnace.getFuel().getItem() == Items.BUCKET) {
            furnace.setStackInSlot(PortableFurnaceInventory.SLOT_FUEL, new ItemStack(Items.WATER_BUCKET));
        }

        input.shrink(1);
        furnace.setStackInSlot(PortableFurnaceInventory.SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
    }
}
