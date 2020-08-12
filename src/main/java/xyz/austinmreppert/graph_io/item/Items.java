package xyz.austinmreppert.graph_io.item;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.block.Blocks;
import xyz.austinmreppert.graph_io.item_group.ItemGroups;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Items {

  public static final Item IDENTIFIER = register(new IdentifierItem(new Item.Properties().group(ItemGroups.GRAPH_IO)), "identifier");
  public static final Item BASIC_ROUTER_CORE = register(new IdentifierItem(new Item.Properties().group(ItemGroups.GRAPH_IO)), "basic_router_core");
  public static final Item ADVANCED_ROUTER_CORE = register(new IdentifierItem(new Item.Properties().group(ItemGroups.GRAPH_IO)), "advanced_router_core");
  public static final Item ELITE_ROUTER_CORE = register(new IdentifierItem(new Item.Properties().group(ItemGroups.GRAPH_IO)), "elite_router_core");
  public static final Item ULTIMATE_ROUTER_CORE = register(new IdentifierItem(new Item.Properties().group(ItemGroups.GRAPH_IO)), "ultimate_router_core");

  @SubscribeEvent
  public static void onRegisterItems(RegistryEvent.Register<Item> itemRegistryEvent) {
    itemRegistryEvent.getRegistry().registerAll(
      IDENTIFIER, BASIC_ROUTER_CORE, ADVANCED_ROUTER_CORE, ELITE_ROUTER_CORE, ULTIMATE_ROUTER_CORE,
      register(new BlockItem(Blocks.BASIC_ROUTER, new Item.Properties().group(ItemGroups.GRAPH_IO)), "basic_router"),
      register(new BlockItem(Blocks.ADVANCED_ROUTER, new Item.Properties().group(ItemGroups.GRAPH_IO)), "advanced_router"),
      register(new BlockItem(Blocks.ELITE_ROUTER, new Item.Properties().group(ItemGroups.GRAPH_IO)), "elite_router"),
      register(new BlockItem(Blocks.ULTIMATE_ROUTER, new Item.Properties().group(ItemGroups.GRAPH_IO)), "ultimate_router")
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
