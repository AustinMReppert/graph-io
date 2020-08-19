package xyz.austinmreppert.graph_io.container;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerTypes {

  public static ContainerType<RouterContainer> ROUTER_CONTAINER = ContainerTypes.register("router");

  @SubscribeEvent
  public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
    event.getRegistry().registerAll(ROUTER_CONTAINER);
  }

  public static <T extends Container> ContainerType<T> register(String name) {
    return (ContainerType<T>) IForgeContainerType.create(RouterContainer::new).setRegistryName(name);
  }


}
