package xyz.austinmreppert.graph_io.container;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import xyz.austinmreppert.graph_io.tileentity.ControllerNodeTE;

import javax.annotation.Nullable;

public class ControllerNodeContainer extends Container {

  private ControllerNodeTE controllerNodeTE;

  private final int HOTBAR_X = 108;
  private final int HOTBAR_Y = 232;
  private final int INVENTORY_X = 108;
  private final int INVENTORY_Y = 174;
  private final int SLOT_SIZE = 18;

  public ControllerNodeContainer(int windowId, PlayerInventory inv, PacketBuffer data) {
    super(ContainerTypes.CONTROLLER_NODE_CONTAINER, windowId);


    TileEntity te = inv.player.world.getTileEntity(data.readBlockPos());
    if(te instanceof ControllerNodeTE)
      this.controllerNodeTE = (ControllerNodeTE) te;

    // Draw the hotbar
    for (int column = 0; column < 9; ++column)
      this.addSlot(new Slot(inv, column, HOTBAR_X + column * SLOT_SIZE, HOTBAR_Y));

    // Draw the player inventory
    for (int row = 0; row < 3; ++row)
      for (int column = 0; column < 9; ++column)
        this.addSlot(new Slot(inv, column + row * 9 + 9, INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));

  }

  public ControllerNodeContainer(int windowId, PlayerInventory inv, ControllerNodeTE controllerNodeTE) {
    super(ContainerTypes.CONTROLLER_NODE_CONTAINER, windowId);

    this.controllerNodeTE = controllerNodeTE;

    // Draw the hotbar
    for (int column = 0; column < 9; ++column)
      this.addSlot(new Slot(inv, column, HOTBAR_X + column * SLOT_SIZE, HOTBAR_Y));

    // Draw the player inventory
    for (int row = 0; row < 3; ++row)
      for (int column = 0; column < 9; ++column)
        this.addSlot(new Slot(inv, column + row * 9 + 9, INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));

  }

  @Override
  public void onContainerClosed(PlayerEntity playerIn) {
    super.onContainerClosed(playerIn);
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    return true;
  }

  @Override
  public ContainerType<?> getType() {
    return ContainerTypes.CONTROLLER_NODE_CONTAINER;
  }

  /**
   * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
   * inventory and the other inventory(s).
   */
  public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
    return ItemStack.EMPTY;
  }

  public ControllerNodeTE getControllerNodeTE() {
    return controllerNodeTE;
  }
}
