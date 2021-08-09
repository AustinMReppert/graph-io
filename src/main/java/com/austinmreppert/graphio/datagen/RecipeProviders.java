package com.austinmreppert.graphio.datagen;

import com.austinmreppert.graphio.block.Blocks;
import com.austinmreppert.graphio.item.Items;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class RecipeProviders extends RecipeProvider {

  public RecipeProviders(DataGenerator dataGenerator) {
    super(dataGenerator);
  }

  @Override
  protected void buildCraftingRecipes(Consumer<FinishedRecipe> finishedRecipes) {
    ShapedRecipeBuilder.shaped(Items.IDENTIFIER)
        .pattern(" p ")
        .pattern(" e ")
        .pattern(" s ")
        .define('p', net.minecraft.world.item.Items.PAPER)
        .define('e', Tags.Items.ENDER_PEARLS)
        .define('s', net.minecraft.world.item.Items.SOUL_TORCH)
        .unlockedBy("has_paper", has(net.minecraft.world.item.Items.PAPER))
        .unlockedBy("has_ender_pearls", has(Tags.Items.ENDER_PEARLS))
        .unlockedBy("has_sould_torch", has(net.minecraft.world.item.Items.SOUL_TORCH))
        .group("tutorial")
        .save(finishedRecipes);

    ShapedRecipeBuilder.shaped(Blocks.BASIC_ROUTER)
        .pattern("iii")
        .pattern("ibi")
        .pattern("ici")
        .define('i', net.minecraft.world.item.Items.IRON_INGOT)
        .define('b', Items.BASIC_ROUTER_CORE)
        .define('c', Items.ROUTER_CIRCUIT)
        .unlockedBy("has_basic_router_core", has(Items.BASIC_ROUTER_CORE))
        .unlockedBy("has_router_circuit", has(Items.ROUTER_CIRCUIT))
        .unlockedBy("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_INGOT))
        .group("tutorial")
        .save(finishedRecipes);

    ShapedRecipeBuilder.shaped(Blocks.ADVANCED_ROUTER)
        .pattern("iii")
        .pattern("ibi")
        .pattern("ici")
        .define('i', net.minecraft.world.item.Items.IRON_INGOT)
        .define('b', Items.ADVANCED_ROUTER_CORE)
        .define('c', Items.ROUTER_CIRCUIT)
        .unlockedBy("has_basic_router_core", has(Items.BASIC_ROUTER_CORE))
        .unlockedBy("has_router_circuit", has(Items.ROUTER_CIRCUIT))
        .unlockedBy("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_INGOT))
        .group("tutorial")
        .save(finishedRecipes);


    ShapedRecipeBuilder.shaped(Blocks.ELITE_ROUTER)
        .pattern("iii")
        .pattern("ibi")
        .pattern("ici")
        .define('i', net.minecraft.world.item.Items.IRON_INGOT)
        .define('b', Items.ELITE_ROUTER_CORE)
        .define('c', Items.ROUTER_CIRCUIT)
        .unlockedBy("has_basic_router_core", has(Items.BASIC_ROUTER_CORE))
        .unlockedBy("has_router_circuit", has(Items.ROUTER_CIRCUIT))
        .unlockedBy("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_INGOT))
        .group("tutorial")
        .save(finishedRecipes);

    ShapedRecipeBuilder.shaped(Blocks.ULTIMATE_ROUTER)
        .pattern("iii")
        .pattern("ibi")
        .pattern("ici")
        .define('i', net.minecraft.world.item.Items.IRON_INGOT)
        .define('b', Items.ULTIMATE_ROUTER_CORE)
        .define('c', Items.ROUTER_CIRCUIT)
        .unlockedBy("has_basic_router_core", has(Items.BASIC_ROUTER_CORE))
        .unlockedBy("has_router_circuit", has(Items.ROUTER_CIRCUIT))
        .unlockedBy("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_INGOT))
        .group("tutorial")
        .save(finishedRecipes);


    ShapedRecipeBuilder.shaped(Items.BASIC_ROUTER_CORE)
        .pattern("ici")
        .pattern("cec")
        .pattern("ici")
        .define('i', net.minecraft.world.item.Items.IRON_INGOT)
        .define('c', net.minecraft.world.item.Items.COAL)
        .define('e', Tags.Items.ENDER_PEARLS)
        .unlockedBy("has_iron_ingot", has(net.minecraft.world.item.Items.IRON_INGOT))
        .unlockedBy("has_coal", has(net.minecraft.world.item.Items.COAL))
        .unlockedBy("has_ender_pearls", has(Tags.Items.ENDER_PEARLS))
        .group("tutorial")
        .save(finishedRecipes);

    ShapedRecipeBuilder.shaped(Items.ADVANCED_ROUTER_CORE)
        .pattern("crc")
        .pattern("rer")
        .pattern("crc")
        .define('r', Items.BASIC_ROUTER_CORE)
        .define('c', net.minecraft.world.item.Items.COPPER_INGOT)
        .define('e', net.minecraft.world.item.Items.ENDER_EYE)
        .unlockedBy("has_copper_ingot", has(net.minecraft.world.item.Items.COPPER_INGOT))
        .unlockedBy("has_basic_router_core", has(Items.BASIC_ROUTER_CORE))
        .unlockedBy("has_ender_eye", has(net.minecraft.world.item.Items.ENDER_EYE))
        .group("tutorial")
        .save(finishedRecipes);

    ShapedRecipeBuilder.shaped(Items.ELITE_ROUTER_CORE)
        .pattern("grg")
        .pattern("rer")
        .pattern("grg")
        .define('r', Items.ADVANCED_ROUTER_CORE)
        .define('g', net.minecraft.world.item.Items.GOLD_INGOT)
        .define('e', net.minecraft.world.item.Items.END_CRYSTAL)
        .unlockedBy("has_gold_ingot", has(net.minecraft.world.item.Items.GOLD_INGOT))
        .unlockedBy("has_advanced_router_core", has(Items.ADVANCED_ROUTER_CORE))
        .unlockedBy("has_end_crystal", has(net.minecraft.world.item.Items.END_CRYSTAL))
        .group("tutorial")
        .save(finishedRecipes);

    ShapedRecipeBuilder.shaped(Items.ULTIMATE_ROUTER_CORE)
        .pattern("nrn")
        .pattern("rer")
        .pattern("nrn")
        .define('r', Items.ELITE_ROUTER_CORE)
        .define('n', net.minecraft.world.item.Items.NETHERITE_INGOT)
        .define('e', net.minecraft.world.item.Items.NETHER_STAR)
        .unlockedBy("has_netherite_ingot", has(net.minecraft.world.item.Items.GOLD_INGOT))
        .unlockedBy("has_elite_router_core", has(Items.ELITE_ROUTER_CORE))
        .unlockedBy("has_nether_star", has(net.minecraft.world.item.Items.NETHER_STAR))
        .group("tutorial")
        .save(finishedRecipes);

    ShapedRecipeBuilder.shaped(Items.ROUTER_CIRCUIT)
        .pattern("pdp")
        .pattern("rer")
        .pattern("coc")
        .define('p', net.minecraft.world.item.Items.REPEATER)
        .define('d', net.minecraft.world.item.Items.DROPPER)
        .define('r', Tags.Items.DUSTS_REDSTONE)
        .define('e', Tags.Items.CHESTS_ENDER)
        .define('c', net.minecraft.world.item.Items.COMPARATOR)
        .define('o', net.minecraft.world.item.Items.OBSERVER)
        .unlockedBy("has_repeater", has(net.minecraft.world.item.Items.REPEATER))
        .unlockedBy("has_dropper", has(net.minecraft.world.item.Items.DROPPER))
        .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
        .unlockedBy("has_ender_chest", has(Tags.Items.CHESTS_ENDER))
        .unlockedBy("has_comparator", has(net.minecraft.world.item.Items.COMPARATOR))
        .unlockedBy("has_observer", has(net.minecraft.world.item.Items.OBSERVER))
        .group("tutorial")
        .save(finishedRecipes);

  }

}
