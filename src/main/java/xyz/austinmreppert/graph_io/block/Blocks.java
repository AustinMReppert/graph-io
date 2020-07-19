package xyz.austinmreppert.graph_io.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import xyz.austinmreppert.graph_io.GraphIO;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Blocks {

  public static final Block CONTROLLER_NODE_BLOCK = register(new ControllerNodeBlock(), "controller_node_block");

  @SubscribeEvent
  public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
    blockRegistryEvent.getRegistry().registerAll(
      CONTROLLER_NODE_BLOCK
    );
  }

  @SubscribeEvent
  public static void onClientSetupEvent(FMLClientSetupEvent event) {
    RenderTypeLookup.setRenderLayer(CONTROLLER_NODE_BLOCK, RenderType.getCutout());
  }

  public static <T extends IForgeRegistryEntry<T>> T register(final T entry, final String name) {
    return register(entry, new ResourceLocation(GraphIO.MOD_ID, name));
  }

  public static <T extends IForgeRegistryEntry<T>> T register(final T entry, final ResourceLocation registryName) {
    entry.setRegistryName(registryName);
    return entry;
  }




}
