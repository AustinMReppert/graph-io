package xyz.austinmreppert.graph_io.container;

import net.minecraft.world.inventory.AbstractContainerMenu;;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContainerTypes {

  public static MenuType<RouterContainer> ROUTER_CONTAINER = ContainerTypes.register("router");

  @SubscribeEvent
  public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
    event.getRegistry().registerAll(ROUTER_CONTAINER);
  }

  public static <T extends AbstractContainerMenu> MenuType<T> register(String name) {
    return (MenuType<T>) IForgeContainerType.create(RouterContainer::new).setRegistryName(name);
  }


}
