package com.austinmreppert.graphio.blockentity;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntities {

  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GraphIO.MOD_ID);

  public static final RegistryObject<BlockEntityType<RouterBlockEntity>> ROUTER = BLOCK_ENTITIES.register("router", () ->
      BlockEntityType.Builder.of(RouterBlockEntity::new, Blocks.BASIC_ROUTER.get(), Blocks.ADVANCED_ROUTER.get(),
          Blocks.ELITE_ROUTER.get(), Blocks.ULTIMATE_ROUTER.get()).build(null));

  public static void register() {
    BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
  }

}
