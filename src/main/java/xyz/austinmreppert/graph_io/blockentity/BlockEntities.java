package xyz.austinmreppert.graph_io.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.austinmreppert.graph_io.block.Blocks;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockEntities {

  @SubscribeEvent
  public static void registerBlockEntity(RegistryEvent.Register<BlockEntityType<?>> blockEntityRegistryEvent) {
    BlockEntityTypes.ROUTER = (BlockEntityType<RouterBlockEntity>) BlockEntityType.Builder.of(RouterBlockEntity::new,
      Blocks.BASIC_ROUTER, Blocks.ADVANCED_ROUTER, Blocks.ELITE_ROUTER,
      Blocks.ULTIMATE_ROUTER).build(null).setRegistryName("router");
    blockEntityRegistryEvent.getRegistry().registerAll(BlockEntityTypes.ROUTER);
  }

}
