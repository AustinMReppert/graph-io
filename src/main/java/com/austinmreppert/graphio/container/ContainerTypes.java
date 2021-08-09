package com.austinmreppert.graphio.container;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.network.IContainerFactory;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerTypes {

  public static MenuType<RouterContainer> ROUTER_CONTAINER = ContainerTypes.register(RouterContainer::new, "router");
  public static MenuType<RouterStorageContainer> ROUTER_STORAGE_CONTAINER = ContainerTypes.register(RouterStorageContainer::new, "router_storage");

  @SubscribeEvent
  public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
    event.getRegistry().registerAll(ROUTER_CONTAINER, ROUTER_STORAGE_CONTAINER);
  }

  public static <T extends AbstractContainerMenu> MenuType<T> register(IContainerFactory<T> factory, String name) {
    return (MenuType<T>) IForgeContainerType.create(factory).setRegistryName(name);
  }


}
