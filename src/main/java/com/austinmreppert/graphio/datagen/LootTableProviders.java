package com.austinmreppert.graphio.datagen;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.block.Blocks;
import com.austinmreppert.graphio.block.RouterBlock;
import com.austinmreppert.graphio.blockentity.BlockEntityTypes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/*
Implementation based off https://forge.gemwire.uk/wiki/Datageneration/Loot_Tables
and https://github.com/McJty/YouTubeTutorial17/blob/main/src/main/java/com/mcjty/datagen/LootTables.java.
*/

/**
 * Generates loot tables.
 */
public class LootTableProviders extends LootTableProvider {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
  protected final Map<Block, LootTable.Builder> lootTables = new HashMap<>();
  public static Map<ResourceLocation, LootTable> tables = new HashMap<>();
  protected final DataGenerator dataGenerator;

  public LootTableProviders(final DataGenerator dataGenerator) {
    super(dataGenerator);
    this.dataGenerator = dataGenerator;
  }

  @Override
  @ParametersAreNonnullByDefault
  public void run(final HashCache hashCache) {
    createRouterLootTable(Blocks.BASIC_ROUTER);
    createRouterLootTable(Blocks.ADVANCED_ROUTER);
    createRouterLootTable(Blocks.ELITE_ROUTER);
    createRouterLootTable(Blocks.ULTIMATE_ROUTER);

    for (final var entry : lootTables.entrySet())
      tables.put(entry.getKey().getLootTable(), entry.getValue().build());

    writeTables(hashCache);
  }

  /**
   * Creates a loot table for a {@link com.austinmreppert.graphio.block.RouterBlock}.
   *
   * @param block The Type of router.
   */
  public void createRouterLootTable(final Block block) {
    if (!(block instanceof RouterBlock))
      throw new IllegalArgumentException("Should only be used with Router Blocks");
    final LootPool.Builder builder = LootPool.lootPool()
        .name(block.getRegistryName().getPath())
        .setRolls(ConstantValue.exactly(1))
        .setBonusRolls(ConstantValue.exactly(0))
        .add(LootItem.lootTableItem(block)
            .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                .copy("Items", "BlockEntityTag.Items", CopyNbtFunction.MergeStrategy.REPLACE)
                .copy("tier", "BlockEntityTag.tier", CopyNbtFunction.MergeStrategy.REPLACE)
                .copy("energyStorage", "BlockEntityTag.energyStorage", CopyNbtFunction.MergeStrategy.REPLACE)
                .copy("mappings", "BlockEntityTag.mappings", CopyNbtFunction.MergeStrategy.REPLACE))
            .apply(SetContainerContents.setContents(BlockEntityTypes.ROUTER)
                .withEntry(DynamicLoot.dynamicEntry(new ResourceLocation("minecraft", "contents")))));
    lootTables.put(block, LootTable.lootTable().withPool(builder));
  }

  /**
   * Writes the loot tables to the generated assets.
   *
   * @param cache The hash cache from run.
   */
  private void writeTables(final HashCache cache) {
    final Path outputFolder = this.dataGenerator.getOutputFolder();
    tables.forEach((final var key, final var lootTable) -> {
      final Path path = outputFolder.resolve("data/" + key.getNamespace() + "/loot_tables/" + key.getPath() + ".json");
      try {
        DataProvider.save(GSON, cache, LootTables.serialize(lootTable), path);
      } catch (final IOException e) {
        GraphIO.LOGGER.error("Couldn't write loot table {}", path);
      }
    });
  }

}
