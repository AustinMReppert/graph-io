package com.austinmreppert.graphio.datagen;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Generates block states/models.
 */
public class BlockStateProviders extends BlockStateProvider {

  public BlockStateProviders(final DataGenerator generator, final ExistingFileHelper existingFileHelper) {
    super(generator, GraphIO.MOD_ID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    simpleBlock(Blocks.BASIC_ROUTER.get());
    simpleBlock(Blocks.ADVANCED_ROUTER.get());
    simpleBlock(Blocks.ELITE_ROUTER.get());
    simpleBlock(Blocks.ULTIMATE_ROUTER.get());

    simpleBlockItem(Blocks.BASIC_ROUTER.get(), models().getExistingFile(new ResourceLocation(
        GraphIO.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(Blocks.BASIC_ROUTER.get()).getPath()
    )));

    simpleBlockItem(Blocks.ADVANCED_ROUTER.get(), models().getExistingFile(new ResourceLocation(
        GraphIO.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(Blocks.ADVANCED_ROUTER.get()).getPath()
    )));

    simpleBlockItem(Blocks.ELITE_ROUTER.get(), models().getExistingFile(new ResourceLocation(
        GraphIO.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(Blocks.ELITE_ROUTER.get()).getPath()
    )));

    simpleBlockItem(Blocks.ULTIMATE_ROUTER.get(), models().getExistingFile(new ResourceLocation(
        GraphIO.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(Blocks.ULTIMATE_ROUTER.get()).getPath()
    )));

  }

}
