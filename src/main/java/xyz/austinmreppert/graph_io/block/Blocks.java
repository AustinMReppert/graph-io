package xyz.austinmreppert.graph_io.block;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.data.tiers.Tier;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Blocks {

  public static final Block BASIC_ROUTER = register(new RouterBlock(Tier.BASIC), "basic_router");
  public static final Block ADVANCED_ROUTER = register(new RouterBlock(Tier.ADVANCED), "advanced_router");
  public static final Block ELITE_ROUTER = register(new RouterBlock(Tier.ELITE), "elite_router");
  public static final Block ULTIMATE_ROUTER = register(new RouterBlock(Tier.ULTIMATE), "ultimate_router");

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
    return register(entry, new ResourceLocation(GraphIO.MOD_ID, name));
  }

  public static <T extends IForgeRegistryEntry<T>> T register(final T entry, final ResourceLocation registryName) {
    entry.setRegistryName(registryName);
    return entry;
  }

}
