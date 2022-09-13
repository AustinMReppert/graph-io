package com.austinmreppert.graphio.item;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.block.Blocks;
import com.austinmreppert.graphio.item_group.ItemGroups;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Items {

  private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GraphIO.MOD_ID);

  public static final RegistryObject<Item> IDENTIFIER = ITEMS.register("identifier", () -> new IdentifierItem(new Item.Properties().tab(ItemGroups.GRAPH_IO)));
    public static final RegistryObject<Item> BASIC_ROUTER_CORE = ITEMS.register("basic_router_core", () -> new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)));
    public static final RegistryObject<Item> ADVANCED_ROUTER_CORE = ITEMS.register("advanced_router_core", () -> new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)));
    public static final RegistryObject<Item> ELITE_ROUTER_CORE = ITEMS.register("elite_router_core", () -> new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)));
    public static final RegistryObject<Item> ULTIMATE_ROUTER_CORE = ITEMS.register("ultimate_router_core", () -> new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)));
    public static final RegistryObject<Item> ROUTER_CIRCUIT = ITEMS.register("router_circuit", () -> new Item(new Item.Properties().tab(ItemGroups.GRAPH_IO)));

    public static final RegistryObject<Item> BASIC_ROUTER = ITEMS.register("basic_router", () ->new BlockItem(Blocks.BASIC_ROUTER.get(), new Item.Properties().tab(ItemGroups.GRAPH_IO)));
    public static final RegistryObject<Item> ADVANCED_ROUTER = ITEMS.register("advanced_router", () -> new BlockItem(Blocks.ADVANCED_ROUTER.get(), new Item.Properties().tab(ItemGroups.GRAPH_IO)));
    public static final RegistryObject<Item> ELITE_ROUTER = ITEMS.register("elite_router", () ->new BlockItem(Blocks.ELITE_ROUTER.get(), new Item.Properties().tab(ItemGroups.GRAPH_IO)));
    public static final RegistryObject<Item> ULTIMATE_ROUTER = ITEMS.register("ultimate_router", () ->new BlockItem(Blocks.ULTIMATE_ROUTER.get(), new Item.Properties().tab(ItemGroups.GRAPH_IO)));

    public static void register() {
      ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
