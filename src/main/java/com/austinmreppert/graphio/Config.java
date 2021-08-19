package com.austinmreppert.graphio;

import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

  public static final ServerConfig SERVER_CONFIG;
  public static final ForgeConfigSpec SERVER_SPEC;

  static {
    final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
    SERVER_CONFIG = specPair.getLeft();
    SERVER_SPEC = specPair.getRight();
  }

  public static final class ServerConfig {

    public final ForgeConfigSpec.IntValue ENERGY_TRANSFER_COST;
    public final ForgeConfigSpec.IntValue ITEM_TRANSFER_COST;
    public final ForgeConfigSpec.IntValue FLUID_TRANSFER_COST;

    public final ForgeConfigSpec.IntValue BASIC_ROUTER_ENERGY_CAPACITY;
    public final ForgeConfigSpec.IntValue ADVANCED_ROUTER_ENERGY_CAPACITY;
    public final ForgeConfigSpec.IntValue ELITE_ROUTER_ENERGY_CAPACITY;
    public final ForgeConfigSpec.IntValue ULTIMATE_ROUTER_ENERGY_CAPACITY;

    public final ForgeConfigSpec.IntValue BASIC_ROUTER_ENERGY_INPUT_PER_UPDATE;
    public final ForgeConfigSpec.IntValue ADVANCED_ROUTER_ENERGY_INPUT_PER_UPDATE;
    public final ForgeConfigSpec.IntValue ELITE_ROUTER_ENERGY_INPUT_PER_UPDATE;
    public final ForgeConfigSpec.IntValue ULTIMATE_ROUTER_ENERGY_INPUT_PER_UPDATE;

    public final ForgeConfigSpec.IntValue BASIC_ROUTER_NUM_MAPPINGS;
    public final ForgeConfigSpec.IntValue ADVANCED_ROUTER_NUM_MAPPINGS;
    public final ForgeConfigSpec.IntValue ELITE_ROUTER_NUM_MAPPINGS;
    public final ForgeConfigSpec.IntValue ULTIMATE_ROUTER_NUM_MAPPINGS;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
      builder.push("routers");

      builder
          .comment("Max number of mappings.")
          .push("mappings");
      BASIC_ROUTER_NUM_MAPPINGS = builder.defineInRange("basic_router", 5, 1, 100);
      ADVANCED_ROUTER_NUM_MAPPINGS = builder.defineInRange("advanced_router", 7, 1, 100);
      ELITE_ROUTER_NUM_MAPPINGS = builder.defineInRange("elite_router", 9, 1, 100);
      ULTIMATE_ROUTER_NUM_MAPPINGS = builder.defineInRange("ultimate_router", 11, 1, 100);
      builder.pop();


      builder
          .comment("Transfer Costs in FE.")
          .push("transfer_costs");

      ITEM_TRANSFER_COST = builder
          .comment("Cost per an item.")
          .translation(GraphIO.MOD_ID + ".config.routers.transfers.item")
          .defineInRange("item", 600, 1, 1000000);
      ENERGY_TRANSFER_COST = builder
          .comment("Efficiency %.")
          .translation(GraphIO.MOD_ID + ".config.routers.transfers.energy")
          .defineInRange("energy", 95, 1, 100);
      FLUID_TRANSFER_COST = builder
          .comment("Cost per a millibucket.")
          .translation(GraphIO.MOD_ID + ".config.routers.transfers.fluid")
          .defineInRange("fluid", 2000, 1, 1000000);
      builder.pop();

      builder
          .comment("Router energy capacity in FE.")
          .push("capacity");
      BASIC_ROUTER_ENERGY_CAPACITY = builder.defineInRange("basic_router", 16000, 1, 1000000);
      ADVANCED_ROUTER_ENERGY_CAPACITY = builder.defineInRange("advanced_router", 32000, 1, 1000000);
      ELITE_ROUTER_ENERGY_CAPACITY = builder.defineInRange("elite_router", 48000, 1, 1000000);
      ULTIMATE_ROUTER_ENERGY_CAPACITY = builder.defineInRange("ultimate_router", 64000, 1, 1000000);
      builder.pop();

      builder
          .comment("The amount of FE a router can input per an update.")
          .push("energy_input");
      BASIC_ROUTER_ENERGY_INPUT_PER_UPDATE = builder.defineInRange("basic_router", 120, 1, 1000000);
      ADVANCED_ROUTER_ENERGY_INPUT_PER_UPDATE = builder.defineInRange("advanced_router", 240, 1, 1000000);
      ELITE_ROUTER_ENERGY_INPUT_PER_UPDATE = builder.defineInRange("elite_router", 480, 1, 1000000);
      ULTIMATE_ROUTER_ENERGY_INPUT_PER_UPDATE = builder.defineInRange("ultimate_router", 960, 1, 1000000);
      builder.pop();

      builder.pop();


    }

  }

  @SubscribeEvent
  public static void onModConfigEvent(final ModConfigEvent configEvent) {
    if (configEvent.getConfig().getSpec() == Config.SERVER_SPEC) {
      RouterBlockEntity.ITEM_TRANSFER_COST = SERVER_CONFIG.ITEM_TRANSFER_COST.get();
      RouterBlockEntity.ENERGY_TRANSFER_COST = SERVER_CONFIG.ENERGY_TRANSFER_COST.get()/100.0F;
      RouterBlockEntity.FLUID_TRANSFER_COST = SERVER_CONFIG.FLUID_TRANSFER_COST.get();
    }
  }



}
