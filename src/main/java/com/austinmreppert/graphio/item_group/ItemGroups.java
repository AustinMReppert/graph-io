package com.austinmreppert.graphio.item_group;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemGroups {

  public static final CreativeModeTab GRAPH_IO = new CreativeModeTab(GraphIO.MOD_ID) {

    @Override
    public ItemStack makeIcon() {
      return new ItemStack(Items.IDENTIFIER);
    }

    @Override
    public boolean hasSearchBar() {
      return false;
    }

    @Override
    public boolean canScroll() {
      return true;
    }

    @Override
    public ResourceLocation getBackgroundImage() {
      return new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    }
  };

}
