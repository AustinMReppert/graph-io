package xyz.austinmreppert.graph_io.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.tileentity.ControllerNodeTE;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Blocks {

  public static final Block WOOD_CONTROLLER_NODE_BLOCK = register(new ControllerNodeBlock(ControllerNodeTE.Tier.WOOD), "wood_controller_node_block");
  public static final Block IRON_CONTROLLER_NODE_BLOCK = register(new ControllerNodeBlock(ControllerNodeTE.Tier.IRON), "iron_controller_node_block");

  @SubscribeEvent
  public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
    blockRegistryEvent.getRegistry().registerAll(
      WOOD_CONTROLLER_NODE_BLOCK,
      IRON_CONTROLLER_NODE_BLOCK
    );
  }

  @SubscribeEvent
  public static void onClientSetupEvent(FMLClientSetupEvent event) {
    RenderTypeLookup.setRenderLayer(WOOD_CONTROLLER_NODE_BLOCK, RenderType.getCutout());
    RenderTypeLookup.setRenderLayer(IRON_CONTROLLER_NODE_BLOCK, RenderType.getCutout());
  }

  public static <T extends IForgeRegistryEntry<T>> T register(final T entry, final String name) {
    return register(entry, new ResourceLocation(GraphIO.MOD_ID, name));
  }

  public static <T extends IForgeRegistryEntry<T>> T register(final T entry, final ResourceLocation registryName) {
    entry.setRegistryName(registryName);
    return entry;
  }

}
