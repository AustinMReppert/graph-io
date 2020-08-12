package xyz.austinmreppert.graph_io.client.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class FilterSlot extends Slot {

  boolean enabled;

  public FilterSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
    enabled = true;
  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean canTakeStack(PlayerEntity playerIn) {
    return false;
  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean isItemValid(ItemStack stack) {
    return true;
  }

  @Override
  @Nonnull
  public ItemStack decrStackSize(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public void putStack(ItemStack stack) {
    ItemStack is = stack.copy();
    if (!is.isEmpty())
      is.setCount(1);
    super.putStack(is);
  }

  @Override
  public int getSlotStackLimit() {
    return 0;
  }

  @Override
  @ParametersAreNonnullByDefault
  public int getItemStackLimit(ItemStack stack) {
    return 1;
  }

  @Override
  @Nonnull
  public ItemStack getStack() {
    return inventory.getStackInSlot(getSlotIndex());
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
