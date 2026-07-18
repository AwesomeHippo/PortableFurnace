package com.awesomehippo.portablefurnace;

import com.awesomehippo.portablefurnace.item.ItemPortableFurnace;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PortableFurnace.MODID);

    public static final RegistryObject<Item> PORTABLE_FURNACE = ITEMS.register("portable_furnace", ItemPortableFurnace::new);
}
