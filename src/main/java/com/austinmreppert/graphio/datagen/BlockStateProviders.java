package com.austinmreppert.graphio.datagen;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateProviders extends BlockStateProvider {

  public BlockStateProviders(DataGenerator generator, ExistingFileHelper existingFileHelper) {
    super(generator, GraphIO.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    simpleBlock(Blocks.BASIC_ROUTER);
    simpleBlock(Blocks.ADVANCED_ROUTER);
    simpleBlock(Blocks.ELITE_ROUTER);
    simpleBlock(Blocks.ULTIMATE_ROUTER);

    simpleBlockItem(Blocks.BASIC_ROUTER, models().getExistingFile(new ResourceLocation(
        GraphIO.MOD_ID, "block/" + Blocks.BASIC_ROUTER.getRegistryName().getPath()
    )));

    simpleBlockItem(Blocks.ADVANCED_ROUTER, models().getExistingFile(new ResourceLocation(
        GraphIO.MOD_ID, "block/" + Blocks.ADVANCED_ROUTER.getRegistryName().getPath()
    )));

    simpleBlockItem(Blocks.ELITE_ROUTER, models().getExistingFile(new ResourceLocation(
        GraphIO.MOD_ID, "block/" + Blocks.ELITE_ROUTER.getRegistryName().getPath()
    )));

    simpleBlockItem(Blocks.ULTIMATE_ROUTER, models().getExistingFile(new ResourceLocation(
        GraphIO.MOD_ID, "block/" + Blocks.ULTIMATE_ROUTER.getRegistryName().getPath()
    )));

  }

}
