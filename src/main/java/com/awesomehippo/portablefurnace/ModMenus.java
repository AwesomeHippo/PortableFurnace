package com.awesomehippo.portablefurnace;

import com.awesomehippo.portablefurnace.inventory.ContainerPortableFurnace;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PortableFurnace.MODID);

    public static final RegistryObject<MenuType<ContainerPortableFurnace>> PORTABLE_FURNACE = MENUS.register("portable_furnace", () -> IForgeMenuType.create(ContainerPortableFurnace::fromNetwork));
}
