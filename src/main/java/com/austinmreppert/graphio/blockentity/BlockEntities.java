package com.austinmreppert.graphio.blockentity;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockEntities {

  @SubscribeEvent
  public static void registerBlockEntity(final RegistryEvent.Register<BlockEntityType<?>> blockEntityRegistryEvent) {
    BlockEntityTypes.ROUTER = (BlockEntityType<RouterBlockEntity>) BlockEntityType.Builder.of(RouterBlockEntity::new,
        Blocks.BASIC_ROUTER, Blocks.ADVANCED_ROUTER, Blocks.ELITE_ROUTER,
        Blocks.ULTIMATE_ROUTER).build(null).setRegistryName("router");
    blockEntityRegistryEvent.getRegistry().registerAll(BlockEntityTypes.ROUTER);
  }

}
