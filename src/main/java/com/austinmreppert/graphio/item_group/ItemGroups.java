package com.austinmreppert.graphio.item_group;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemGroups {

  public static final CreativeModeTab GRAPH_IO = new CreativeModeTab(GraphIO.MOD_ID) {

    private static final ResourceLocation BACKGROUND_IMAGE = new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");

    /**
     * Creates an ItemStack to be used for the tab's icon.
     * @return An ItemStack to be used for the tab's icon.
     */
    @Override
    public ItemStack makeIcon() {
      return new ItemStack(Items.IDENTIFIER);
    }

    /**
     * Gets whether the search feature should be enabled.
     * @return Whether the search feature should be enabled.
     */
    @Override
    public boolean hasSearchBar() {
      return true;
    }

    /**
     * Gets the texture for the tab.
     * @return The texture for the tab.
     */
    @Override
    public ResourceLocation getBackgroundImage() {
      return BACKGROUND_IMAGE;
    }
  };

}
