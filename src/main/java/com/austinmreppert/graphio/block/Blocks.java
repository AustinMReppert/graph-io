package com.austinmreppert.graphio.block;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.data.tiers.BaseTier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Blocks {

  public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GraphIO.MOD_ID);
  public static final RegistryObject<Block> BASIC_ROUTER = BLOCKS.register("basic_router", () -> new RouterBlock(BaseTier.BASIC));
  public static final RegistryObject<Block> ADVANCED_ROUTER = BLOCKS.register("advanced_router", () -> new RouterBlock(BaseTier.ADVANCED));
  public static final RegistryObject<Block> ELITE_ROUTER = BLOCKS.register("elite_router", () -> new RouterBlock(BaseTier.ELITE));
  public static final RegistryObject<Block> ULTIMATE_ROUTER = BLOCKS.register("ultimate_router", () -> new RouterBlock(BaseTier.ULTIMATE));

  public static void register() {
    BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
  }

}
