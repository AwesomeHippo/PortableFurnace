package com.awesomehippo.portablefurnace;

import com.awesomehippo.portablefurnace.inventory.GuiHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = PortableFurnace.MODID, name = PortableFurnace.NAME, version = PortableFurnace.VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class PortableFurnace {

    public static final String MODID = "portablefurnace";
    public static final String NAME = "Portable Furnace";
    public static final String VERSION = "3.0";

    public static final int GUI_PORTABLE_FURNACE = 0;

    @Mod.Instance(MODID)
    public static PortableFurnace instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }
}
