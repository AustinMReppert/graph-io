package com.austinmreppert.graphio.datagen;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.item.Items;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Generates item models.
 */
public class ItemModelProviders extends ItemModelProvider {

  public ItemModelProviders(final DataGenerator dataGenerator, final ExistingFileHelper existingFileHelper) {
    super(dataGenerator, GraphIO.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    singleTexture(Items.IDENTIFIER.getRegistryName().getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + Items.IDENTIFIER.getRegistryName().getPath()));

    singleTexture(Items.BASIC_ROUTER_CORE.getRegistryName().getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + Items.BASIC_ROUTER_CORE.getRegistryName().getPath()));


    singleTexture(Items.ADVANCED_ROUTER_CORE.getRegistryName().getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + Items.ADVANCED_ROUTER_CORE.getRegistryName().getPath()));


    singleTexture(Items.ELITE_ROUTER_CORE.getRegistryName().getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + Items.ELITE_ROUTER_CORE.getRegistryName().getPath()));

    singleTexture(Items.ULTIMATE_ROUTER_CORE.getRegistryName().getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + Items.ULTIMATE_ROUTER_CORE.getRegistryName().getPath()));

    singleTexture(Items.ROUTER_CIRCUIT.getRegistryName().getPath(),
        new ResourceLocation("item/generated"), "layer0",
        new ResourceLocation(GraphIO.MOD_ID, "item/" + Items.ROUTER_CIRCUIT.getRegistryName().getPath()));
  }

}
