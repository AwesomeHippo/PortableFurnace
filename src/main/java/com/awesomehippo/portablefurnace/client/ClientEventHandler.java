package com.awesomehippo.portablefurnace.client;

import com.awesomehippo.portablefurnace.ModItems;
import com.awesomehippo.portablefurnace.PortableFurnace;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = PortableFurnace.MODID)
public final class ClientEventHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (ModItems.PORTABLE_FURNACE != null && ModItems.PORTABLE_FURNACE.getRegistryName() != null) {
            ModelLoader.setCustomModelResourceLocation(ModItems.PORTABLE_FURNACE, 0, new ModelResourceLocation(ModItems.PORTABLE_FURNACE.getRegistryName(), "inventory"));
        }
    }
}
