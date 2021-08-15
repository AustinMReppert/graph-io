package com.austinmreppert.graphio.client.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link Slot} used for filters. Items are copied to the slot, but cannot be retrieved.
 */
public class FilterSlot extends Slot {

  private boolean enabled;

  public FilterSlot(final Container inventoryIn, final int index, final int xPosition, final int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
    enabled = true;
  }

  /**
   * Gets whether the player can pick up the item in the slot.
   *
   * @param player The player trying to pick up the item.
   * @return Whether the player can pick up the item in the slot.
   */
  @Override
  @ParametersAreNonnullByDefault
  public boolean mayPickup(final Player player) {
    return false;
  }

  /**
   * Gets whether the {@link ItemStack} may be placed into this slot.
   *
   * @param stack The item.
   * @return Whether the {@link ItemStack} may be placed into this slot.
   */
  @Override
  @ParametersAreNonnullByDefault
  public boolean mayPlace(final ItemStack stack) {
    return true;
  }

  /**
   * Removes an item from the slot.
   *
   * @param amount The amount of items to remove.
   * @return The resulting {@link ItemStack}.
   */
  @Override
  @Nonnull
  public ItemStack remove(final int amount) {
    return ItemStack.EMPTY;
  }

  /**
   * Sets the contents of the slot.
   *
   * @param stack The item to use.
   */
  @Override
  public void set(final ItemStack stack) {
    ItemStack is = stack.copy();
    if (!is.isEmpty())
      is.setCount(1);
    super.set(is);
  }

  /**
   * Gets the maximum size of an {@link ItemStack} that can be used in the slot.
   *
   * @return The maximum size of an {@link ItemStack} that can be used in the slot
   */
  @Override
  public int getMaxStackSize() {
    return 0;
  }

  /**
   * Gets the maximum size of the slot containing some item.
   *
   * @param stack The item.
   * @return The maximum size of the slot contain {@code stack}.
   */
  @Override
  @ParametersAreNonnullByDefault
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }

  /**
   * Gets the current {@link ItemStack} stored by this slot.
   *
   * @return The current {@link ItemStack} stored by this slot.
   */
  @Override
  @Nonnull
  public ItemStack getItem() {
    return container.getItem(getSlotIndex());
  }

  /**
   * Get whether the slot is enabled.
   *
   * @return whether the slot is enabled.
   */
  @Override
  public boolean isActive() {
    return enabled;
  }

  /**
   * Sets whether the slot is enabled.
   *
   * @param enabled Whether the slot is enabled.
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
