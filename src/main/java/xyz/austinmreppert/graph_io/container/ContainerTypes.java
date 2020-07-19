package xyz.austinmreppert.graph_io.container;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerTypes {

  public static ContainerType<ControllerNodeContainer> CONTROLLER_NODE_CONTAINER = ContainerTypes.register("example_block");

  @SubscribeEvent
  public static void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
    event.getRegistry().registerAll(CONTROLLER_NODE_CONTAINER);
  }

  public static <T extends Container> ContainerType<T> register(String name) {
    return (ContainerType<T>) IForgeContainerType.create(ControllerNodeContainer::new).setRegistryName(name);
  }


}
