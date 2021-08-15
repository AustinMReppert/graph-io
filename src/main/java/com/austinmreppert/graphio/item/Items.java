package com.austinmreppert.graphio.item;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.block.Blocks;
import com.austinmreppert.graphio.item_group.ItemGroups;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Items {

  public static final Item IDENTIFIER = register(new IdentifierItem(new Item.Properties().tab(ItemGroups.GRAPH_IO)), "identifier");
  public static final Item BASIC_ROUTER_CORE = register(new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)), "basic_router_core");
  public static final Item ADVANCED_ROUTER_CORE = register(new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)), "advanced_router_core");
  public static final Item ELITE_ROUTER_CORE = register(new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)), "elite_router_core");
  public static final Item ULTIMATE_ROUTER_CORE = register(new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)), "ultimate_router_core");
  public static final Item ROUTER_CIRCUIT = register(new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)), "router_circuit");

  @SubscribeEvent
  public static void onRegisterItems(final RegistryEvent.Register<Item> itemRegistryEvent) {
    itemRegistryEvent.getRegistry().registerAll(
        IDENTIFIER, BASIC_ROUTER_CORE, ADVANCED_ROUTER_CORE, ELITE_ROUTER_CORE, ULTIMATE_ROUTER_CORE, ROUTER_CIRCUIT,
        register(new BlockItem(Blocks.BASIC_ROUTER, new Item.Properties().tab(ItemGroups.GRAPH_IO)), "basic_router"),
        register(new BlockItem(Blocks.ADVANCED_ROUTER, new Item.Properties().tab(ItemGroups.GRAPH_IO)), "advanced_router"),
        register(new BlockItem(Blocks.ELITE_ROUTER, new Item.Properties().tab(ItemGroups.GRAPH_IO)), "elite_router"),
        register(new BlockItem(Blocks.ULTIMATE_ROUTER, new Item.Properties().tab(ItemGroups.GRAPH_IO)), "ultimate_router")
    );

  }

  public static <T extends IForgeRegistryEntry<T>> T register(final T entry, final String name) {
    return entry.setRegistryName(new ResourceLocation(GraphIO.MOD_ID, name));
  }

}
