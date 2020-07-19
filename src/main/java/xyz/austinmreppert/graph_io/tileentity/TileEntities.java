package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.austinmreppert.graph_io.block.Blocks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TileEntities {

  @SubscribeEvent
  public static void registerTE(RegistryEvent.Register<TileEntityType<?>> tileEntityRegistryEvent) {
    TileEntityTypes.CONTROLLER_NODE = (TileEntityType<ControllerNodeTE>) TileEntityType.Builder.create(ControllerNodeTE::new, Blocks.CONTROLLER_NODE_BLOCK).build(null).setRegistryName("example_block");
    tileEntityRegistryEvent.getRegistry().registerAll(TileEntityTypes.CONTROLLER_NODE);
  }

  public static TileEntityType<?> register() {
    return null;
  }

}
