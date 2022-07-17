package com.austinmreppert.graphio.container;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ContainerTypes {

  private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, GraphIO.MOD_ID);

  public static final RegistryObject<MenuType<RouterContainer>> ROUTER_CONTAINER = CONTAINERS.register("router",
      () -> IForgeMenuType.create((windowID, inventory, data) -> {
        return new RouterContainer(windowID, inventory, (RouterBlockEntity) inventory.player.getLevel().getBlockEntity(data.readBlockPos()));
      }));

  public static final RegistryObject<MenuType<RouterStorageContainer>> ROUTER_STORAGE_CONTAINER = CONTAINERS.register("router_storage",
      () -> IForgeMenuType.create((windowID, inventory, data) -> {
        final var router = (RouterBlockEntity) inventory.player.getLevel().getBlockEntity(data.readBlockPos());
        return router != null ? new RouterStorageContainer(windowID, inventory, router) : null;
      }));

  public static void register() {
    CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
  }

}
