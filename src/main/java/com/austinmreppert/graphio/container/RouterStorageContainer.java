package com.austinmreppert.graphio.container;

import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Synchronizes data between client and server for the {@link com.austinmreppert.graphio.client.gui.RouterStorageScreen}.
 */
public class RouterStorageContainer extends AbstractContainerMenu {

  private static final int SLOTS_PER_ROW = 3;
  private final int containerRows;
  private final RouterBlockEntity routerBlockEntity;
  public static final int SLOT_SIZE = 18;

  public RouterStorageContainer(final int windowID, final Inventory playerInventory, final FriendlyByteBuf data) {
    this(windowID, playerInventory, (RouterBlockEntity) playerInventory.player.level.getBlockEntity(data.readBlockPos()));
  }

  public RouterStorageContainer(final int windowID, final Inventory playerInventory, final RouterBlockEntity routerBlockEntity) {
    super(ContainerTypes.ROUTER_STORAGE_CONTAINER, windowID);
    this.routerBlockEntity = routerBlockEntity;

    final int rows = routerBlockEntity.getContainerSize() / 3;
    this.containerRows = rows;
    final int startY = (this.containerRows - 4) * SLOT_SIZE;

    routerBlockEntity.startOpen(playerInventory.player);

    int row;
    int column;
    // Draw the router storage
    for (row = 0; row < rows; ++row) {
      for (column = 0; column < 3; ++column) {
        this.addSlot(new Slot(routerBlockEntity, column + row * 3, 62 + column * SLOT_SIZE, SLOT_SIZE + row * SLOT_SIZE));
      }
    }

    // Draw the player's inventory
    for (row = 0; row < 3; ++row) {
      for (column = 0; column < 9; ++column) {
        this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * SLOT_SIZE, 103 + row * SLOT_SIZE + startY));
      }
    }

    // Draw the player's hotbar
    for (row = 0; row < 9; ++row) {
      this.addSlot(new Slot(playerInventory, row, 8 + row * SLOT_SIZE, 161 + startY));
    }

  }

  /**
   * Gets whether the container is still valid.
   *
   * @param player The player using the container.
   * @return Whether the container is still valid.
   */
  @Override
  public boolean stillValid(final Player player) {
    return this.routerBlockEntity.stillValid(player);
  }

  /**
   * Handles shift-clicking items.
   *
   * @param player The player attempting to moving items.
   * @param slotID The slot of the shift-clicked item.
   * @return The resulting {@link ItemStack}.
   */
  @Override
  public ItemStack quickMoveStack(final Player player, final int slotID) {
    ItemStack tmp = ItemStack.EMPTY;
    final Slot slot = this.slots.get(slotID);
    if (slot != null && slot.hasItem()) {
      final ItemStack is = slot.getItem();
      tmp = is.copy();
      if (slotID < this.containerRows * SLOTS_PER_ROW) {
        if (!this.moveItemStackTo(is, this.containerRows * SLOTS_PER_ROW, this.slots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (!this.moveItemStackTo(is, 0, this.containerRows * SLOTS_PER_ROW, false)) {
        return ItemStack.EMPTY;
      }

      if (is.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }

    return tmp;
  }

  /**
   * Handles when a Player is dislodged from a container.
   *
   * @param player Handles when a Player is dislodged from a container.
   */
  @Override
  @ParametersAreNonnullByDefault
  public void removed(Player player) {
    super.removed(player);
    this.routerBlockEntity.stopOpen(player);
  }

  /**
   * Gets the {@link RouterBlockEntity} associated with this container.
   *
   * @return The {@link RouterBlockEntity} associated with this container.
   */
  public Container getRouterBlockEntity() {
    return this.routerBlockEntity;
  }

  /**
   * Get the amount of rows this container has.
   *
   * @return The amount of rows this container has.
   */
  public int getRowCount() {
    return this.containerRows;
  }

}
