package com.awesomehippo.portablefurnace;

import com.awesomehippo.portablefurnace.item.ItemPortableFurnace;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = PortableFurnace.MODID)
public final class ModItems {

    @GameRegistry.ObjectHolder(PortableFurnace.MODID + ":portable_furnace")
    public static final Item PORTABLE_FURNACE = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        Item portableFurnace = new ItemPortableFurnace().setRegistryName(PortableFurnace.MODID, "portable_furnace").setUnlocalizedName(PortableFurnace.MODID + ".portable_furnace").setCreativeTab(CreativeTabs.TOOLS);

        registry.register(portableFurnace);
    }
}
