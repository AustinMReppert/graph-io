package com.austinmreppert.graphio.datagen;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.item.Items;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Generates item models.
 */
public class ItemModelProviders extends ItemModelProvider {

  public ItemModelProviders(final DataGenerator dataGenerator, final ExistingFileHelper existingFileHelper) {
    super(dataGenerator, GraphIO.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    singleTexture(ForgeRegistries.ITEMS.getKey(Items.IDENTIFIER.get()).getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + ForgeRegistries.ITEMS.getKey(Items.IDENTIFIER.get()).getPath()));

    singleTexture(ForgeRegistries.ITEMS.getKey(Items.BASIC_ROUTER_CORE.get()).getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + ForgeRegistries.ITEMS.getKey(Items.BASIC_ROUTER_CORE.get()).getPath()));


    singleTexture(ForgeRegistries.ITEMS.getKey(Items.ADVANCED_ROUTER_CORE.get()).getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + ForgeRegistries.ITEMS.getKey(Items.ADVANCED_ROUTER_CORE.get()).getPath()));


    singleTexture(ForgeRegistries.ITEMS.getKey(Items.ELITE_ROUTER_CORE.get()).getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + ForgeRegistries.ITEMS.getKey(Items.ELITE_ROUTER_CORE.get()).getPath()));

    singleTexture(ForgeRegistries.ITEMS.getKey(Items.ULTIMATE_ROUTER_CORE.get()).getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + ForgeRegistries.ITEMS.getKey(Items.ULTIMATE_ROUTER_CORE.get()).getPath()));

    singleTexture(ForgeRegistries.ITEMS.getKey(Items.ROUTER_CIRCUIT.get()).getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + ForgeRegistries.ITEMS.getKey(Items.ROUTER_CIRCUIT.get()).getPath()));
  }

}
