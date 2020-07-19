package xyz.austinmreppert.graph_io.item_group;

import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import xyz.austinmreppert.graph_io.GraphIO;

public class ItemGroups {

  public static final ItemGroup GRAPH_IO = new ItemGroup(GraphIO.MOD_ID) {

    @Override
    public ItemStack createIcon() {
      return new ItemStack(Items.LAPIS_BLOCK);
    }

    @Override
    public boolean hasSearchBar() {
      return true;
    }

    @Override
    public boolean hasScrollbar() {
      return true;
    }

    @Override
    public ResourceLocation getBackgroundImage() {
      return new ResourceLocation("textures/gui/container/creative_inventory/tab_item_search.png");
    }
  };

}
