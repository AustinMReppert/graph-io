package xyz.austinmreppert.graph_io.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.data.tiers.BaseTier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Blocks {

  public static final Block BASIC_ROUTER = register(new RouterBlock(BaseTier.BASIC), "basic_router");
  public static final Block ADVANCED_ROUTER = register(new RouterBlock(BaseTier.ADVANCED), "advanced_router");
  public static final Block ELITE_ROUTER = register(new RouterBlock(BaseTier.ELITE), "elite_router");
  public static final Block ULTIMATE_ROUTER = register(new RouterBlock(BaseTier.ULTIMATE), "ultimate_router");

  @SubscribeEvent
  public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
    blockRegistryEvent.getRegistry().registerAll(
      BASIC_ROUTER,
      ADVANCED_ROUTER,
      ELITE_ROUTER,
      ULTIMATE_ROUTER
    );
  }

  public static <T extends IForgeRegistryEntry<T>> T register(final T entry, final String name) {
    return entry.setRegistryName(new ResourceLocation(GraphIO.MOD_ID, name));
  }


}
