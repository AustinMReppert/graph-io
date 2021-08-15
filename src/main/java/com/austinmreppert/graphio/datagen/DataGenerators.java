package com.austinmreppert.graphio.datagen;

import com.austinmreppert.graphio.GraphIO;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

/**
 * Generates assets.
 */
@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

  @SubscribeEvent
  public static void onGatherData(final GatherDataEvent event) {
    if (event.includeClient()) {
      final var generator = event.getGenerator();
      final var existingFileHelper = event.getExistingFileHelper();
      generator.addProvider(new ItemModelProviders(generator, existingFileHelper));
      generator.addProvider(new BlockStateProviders(generator, existingFileHelper));
      generator.addProvider(new RecipeProviders(generator));
      generator.addProvider(new LootTableProviders(generator));
    }
  }

}
