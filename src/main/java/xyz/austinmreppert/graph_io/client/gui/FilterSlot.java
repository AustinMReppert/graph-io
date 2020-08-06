package xyz.austinmreppert.graph_io.client.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import xyz.austinmreppert.graph_io.container.ControllerNodeContainer;

public class FilterSlot extends Slot {

  public FilterSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  @Override
  public boolean canTakeStack(PlayerEntity playerIn) {
    return false;
  }

  @Override
  public boolean isItemValid(ItemStack stack) {
    return true;
  }

  @Override
  public ItemStack decrStackSize(int amount) {
    return ItemStack.EMPTY;
  }

  @Override
  public void putStack(ItemStack stack) {
    ItemStack is = stack.copy();
    if(!is.isEmpty())
      is.setCount(1);
    super.putStack(is);
  }

  @Override
  public int getSlotStackLimit() {
    return 0;
  }

  @Override
  public int getItemStackLimit(ItemStack stack) {
    return 1;
  }

  @Override
  public ItemStack getStack() {
    return inventory.getStackInSlot(getSlotIndex());
  }
}
