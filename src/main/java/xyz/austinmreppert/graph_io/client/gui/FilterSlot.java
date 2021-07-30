package xyz.austinmreppert.graph_io.client.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class FilterSlot extends Slot {

  private boolean enabled;

  public FilterSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
    enabled = true;
  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean mayPickup(Player playerIn) {
    return false;
  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean mayPlace(ItemStack stack) {
    return true;
  }

  @Override
  @Nonnull
  public ItemStack remove(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public void set(ItemStack stack) {
    ItemStack is = stack.copy();
    if (!is.isEmpty())
      is.setCount(1);
    super.set(is);
  }

  @Override
  public int getMaxStackSize() {
    return 0;
  }

  @Override
  @ParametersAreNonnullByDefault
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }

  @Override
  @Nonnull
  public ItemStack getItem() {
    return container.getItem(getSlotIndex());
  }

  @Override
  public boolean isActive() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
