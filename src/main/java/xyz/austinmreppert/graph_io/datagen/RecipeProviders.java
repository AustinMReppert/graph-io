package xyz.austinmreppert.graph_io.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraftforge.common.Tags;
import xyz.austinmreppert.graph_io.item.Items;

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
  }

}
