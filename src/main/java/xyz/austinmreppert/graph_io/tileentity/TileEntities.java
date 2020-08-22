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
    TileEntityTypes.ROUTER = (TileEntityType<RouterTE>) TileEntityType.Builder.create(RouterTE::new,
      Blocks.BASIC_ROUTER, Blocks.ADVANCED_ROUTER, Blocks.ELITE_ROUTER,
      Blocks.ULTIMATE_ROUTER).build(null).setRegistryName("router");
    tileEntityRegistryEvent.getRegistry().registerAll(TileEntityTypes.ROUTER);
  }

}
