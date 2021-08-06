package xyz.austinmreppert.graph_io.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.austinmreppert.graph_io.blockentity.RouterBlockEntity;

public class RouterStorageContainer extends AbstractContainerMenu {

  private static final int SLOTS_PER_ROW = 3;
  private final Container container;
  private final int containerRows;
  private RouterBlockEntity routerBlockEntity;
  private static final int SLOT_SIZE = 18;

  public RouterStorageContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
    this(windowId, inv, (RouterBlockEntity) inv.player.level.getBlockEntity(data.readBlockPos()));
  }


  public RouterStorageContainer(int windowID, Inventory playerInventory, RouterBlockEntity routerBlockEntity) {
    super(ContainerTypes.ROUTER_STORAGE_CONTAINER, windowID);
    this.routerBlockEntity = routerBlockEntity;
    int rows = routerBlockEntity.getContainerSize()/3;
    this.container = routerBlockEntity;
    this.containerRows = rows;
    container.startOpen(playerInventory.player);
    int startY = (this.containerRows - 4) * SLOT_SIZE;

    int row;
    int column;
    for(row = 0; row < rows; ++row) {
      for(column = 0; column < 3; ++column) {
        this.addSlot(new Slot(routerBlockEntity, column + row * 3, 62 + column * SLOT_SIZE, SLOT_SIZE + row * SLOT_SIZE));
      }
    }

    for(row = 0; row < 3; ++row) {
      for(column = 0; column < 9; ++column) {
        this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * SLOT_SIZE, 103 + row * SLOT_SIZE + startY));
      }
    }

    for(row = 0; row < 9; ++row) {
      this.addSlot(new Slot(playerInventory, row, 8 + row * SLOT_SIZE, 161 + startY));
    }

  }

  public boolean stillValid(Player player) {
    return this.container.stillValid(player);
  }

  public ItemStack quickMoveStack(Player player, int slotID) {
    ItemStack tmp = ItemStack.EMPTY;
    Slot slot = (Slot)this.slots.get(slotID);
    if (slot != null && slot.hasItem()) {
      ItemStack is = slot.getItem();
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

  public void removed(Player player) {
    super.removed(player);
    this.container.stopOpen(player);
  }

  public Container getContainer() {
    return this.container;
  }

  public int getRowCount() {
    return this.containerRows;
  }

}
