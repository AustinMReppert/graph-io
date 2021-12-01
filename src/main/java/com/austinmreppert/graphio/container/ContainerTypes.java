package com.austinmreppert.graphio.container;

import com.austinmreppert.graphio.GraphIO;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerTypes {

  public static final MenuType<RouterContainer> ROUTER_CONTAINER = ContainerTypes.register(RouterContainer::new, "router");
  public static final MenuType<RouterStorageContainer> ROUTER_STORAGE_CONTAINER = ContainerTypes.register(RouterStorageContainer::new, "router_storage");

  @SubscribeEvent
  public static void registerContainers(final RegistryEvent.Register<MenuType<?>> event) {
    event.getRegistry().registerAll(ROUTER_CONTAINER, ROUTER_STORAGE_CONTAINER);
  }

  public static <T extends AbstractContainerMenu> MenuType<T> register(final IContainerFactory<T> factory, final String name) {
    return (MenuType<T>) IForgeMenuType.create(factory).setRegistryName(name);
  }


}
