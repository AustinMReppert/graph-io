package com.austinmreppert.graphio.datagen;

import com.austinmreppert.graphio.block.Blocks;
import com.austinmreppert.graphio.block.RouterBlock;
import com.austinmreppert.graphio.blockentity.BlockEntities;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
Implementation based off https://forge.gemwire.uk/wiki/Datageneration/Loot_Tables
and https://github.com/McJty/YouTubeTutorial17/blob/main/src/main/java/com/mcjty/datagen/LootTables.java.
*/

/**
 * Generates loot tables.
 */
public class LootTableProviders extends LootTableProvider {

  @Override
  protected void validate(Map<ResourceLocation, LootTable> tables, ValidationContext ctx) {
    tables.forEach((name, table) -> LootTables.validate(ctx, name, table));
  }

  public LootTableProviders(final DataGenerator dataGenerator) {
    super(dataGenerator);
  }

  @Override
  protected @NotNull List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
    return ImmutableList.of(
        Pair.of(RouterLoot::new, LootContextParamSets.BLOCK)
    );
  }

  public static class RouterLoot extends BlockLoot {

    /**
     * Creates a loot table for a {@link com.austinmreppert.graphio.block.RouterBlock}.
     *
     * @param block The Type of router.
     */
    public void createRouterLootPool(final Block block) {
      if (!(block instanceof RouterBlock))
        throw new IllegalArgumentException("Should only be used with Router Blocks");
      final LootPool.Builder builder = LootPool.lootPool()
          .name(ForgeRegistries.BLOCKS.getKey(block).getPath())
          .setRolls(ConstantValue.exactly(1))
          .setBonusRolls(ConstantValue.exactly(0))
          .add(LootItem.lootTableItem(block)
              .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                  .copy("Items", "BlockEntityTag.Items", CopyNbtFunction.MergeStrategy.REPLACE)
                  .copy("tier", "BlockEntityTag.tier", CopyNbtFunction.MergeStrategy.REPLACE)
                  .copy("energyStorage", "BlockEntityTag.energyStorage", CopyNbtFunction.MergeStrategy.REPLACE)
                  .copy("mappings", "BlockEntityTag.mappings", CopyNbtFunction.MergeStrategy.REPLACE))
              .apply(SetContainerContents.setContents(BlockEntities.ROUTER.get())
                  .withEntry(DynamicLoot.dynamicEntry(new ResourceLocation("minecraft", "contents")))));

      add(block, LootTable.lootTable().withPool(builder));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
      return Blocks.BLOCKS.getEntries()
          .stream()
          .flatMap(RegistryObject::stream)
          ::iterator;
    }

    @Override
    protected void addTables() {
      createRouterLootPool(Blocks.BASIC_ROUTER.get());
      createRouterLootPool(Blocks.ADVANCED_ROUTER.get());
      createRouterLootPool(Blocks.ELITE_ROUTER.get());
      createRouterLootPool(Blocks.ULTIMATE_ROUTER.get());
    }
  }

}
