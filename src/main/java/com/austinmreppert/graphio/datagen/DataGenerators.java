package com.austinmreppert.graphio.datagen;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {


  @SubscribeEvent
  public static void onGatherData(GatherDataEvent event) {
    if(event.includeClient()) {
      var generator = event.getGenerator();
      var existingFileHelper = event.getExistingFileHelper();
      generator.addProvider(new ItemModelProviders(generator, existingFileHelper));
      generator.addProvider(new BlockStateProviders(generator, existingFileHelper));
      generator.addProvider(new RecipeProviders(generator));
      generator.addProvider(new LootTableProviders(generator));
    }
  }

}
