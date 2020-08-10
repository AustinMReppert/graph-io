package xyz.austinmreppert.graph_io.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import xyz.austinmreppert.graph_io.client.gui.FilterSlot;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

public class RouterContainer extends Container {

  private final RouterTE routerTE;
  private final ArrayList<FilterSlot> filterSlots;
  private final Inventory tmpFilterInventory;

  private final int HOTBAR_X = 108;
  private final int HOTBAR_Y = 232;
  private final int INVENTORY_X = 108;
  private final int INVENTORY_Y = 174;
  private final int SLOT_SIZE = 18;

  public RouterContainer(int windowId, PlayerInventory inv, PacketBuffer data) {
    this(windowId, inv, (RouterTE) inv.player.world.getTileEntity(data.readBlockPos()));
  }

  public RouterContainer(int windowId, PlayerInventory inv, RouterTE routerTE) {
    super(ContainerTypes.CONTROLLER_NODE_CONTAINER, windowId);

    this.routerTE = routerTE;

    tmpFilterInventory = new Inventory(routerTE.getFilterSize());
    filterSlots = new ArrayList<>();

    // Draw the hotbar
    for (int column = 0; column < 9; ++column)
      this.addSlot(new Slot(inv, column, HOTBAR_X + column * SLOT_SIZE, HOTBAR_Y));

    // Draw the player inventory
    for (int row = 0; row < 3; ++row)
      for (int column = 0; column < 9; ++column)
        this.addSlot(new Slot(inv, column + row * 9 + 9, INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));

    // Draw the filter slots
    for (int i = 0; i < tmpFilterInventory.getSizeInventory(); ++i)
      filterSlots.add((FilterSlot) addSlot(new FilterSlot(tmpFilterInventory, i, 5 + (i % 5) * SLOT_SIZE, INVENTORY_Y + (i >= 5 ? SLOT_SIZE : 0))));
  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean canDragIntoSlot(Slot slot) {
    if (slot instanceof FilterSlot) return false;
    return super.canDragIntoSlot(slot);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
    Slot slot = slotId < 0 ? null : getSlot(slotId);
    if (slot instanceof FilterSlot) {
      if (clickType == ClickType.PICKUP && player.inventory.getItemStack().isEmpty())
        slot.putStack(ItemStack.EMPTY);
      else
        slot.putStack(player.inventory.getItemStack().isEmpty() ? ItemStack.EMPTY : player.inventory.getItemStack().copy());
      return player.inventory.getItemStack();
    } else
      return super.slotClick(slotId, dragType, clickType, player);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
    return ItemStack.EMPTY;
  }

  @Override
  @ParametersAreNonnullByDefault
  public void onContainerClosed(PlayerEntity player) {
    super.onContainerClosed(player);
  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean canInteractWith(PlayerEntity player) {
    return true;
  }

  @Override
  @Nonnull
  public ContainerType<?> getType() {
    return ContainerTypes.CONTROLLER_NODE_CONTAINER;
  }

  public RouterTE getControllerNodeTE() {
    return routerTE;
  }

  public ArrayList<FilterSlot> getFilterSlots() {
    return filterSlots;
  }

  public Inventory getTmpFilterInventory() {
    return tmpFilterInventory;
  }

}
