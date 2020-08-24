package xyz.austinmreppert.graph_io.container;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.fml.network.NetworkDirection;
import xyz.austinmreppert.graph_io.client.gui.FilterSlot;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;
import xyz.austinmreppert.graph_io.network.PacketHander;
import xyz.austinmreppert.graph_io.network.SetMappingsPacket;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class RouterContainer extends Container {

  private final List<ServerPlayerEntity> listeners;
  private final MappingsReferenceHolder trackedMappingsReference;

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
    trackedMappingsReference.set.accept(data.readCompoundTag());
  }

  public RouterContainer(int windowId, PlayerInventory inv, RouterTE routerTE) {
    super(ContainerTypes.ROUTER_CONTAINER, windowId);

    listeners = Lists.newArrayList();

    this.routerTE = routerTE;

    tmpFilterInventory = new Inventory(routerTE.getTier().filterSize);
    filterSlots = new ArrayList<>();

    // Draw the hotbar
    for (int column = 0; column < 9; ++column)
      this.addSlot(new Slot(inv, column, HOTBAR_X + column * SLOT_SIZE, HOTBAR_Y));

    // Draw the player inventory
    for (int row = 0; row < 3; ++row)
      for (int column = 0; column < 9; ++column)
        this.addSlot(new Slot(inv, column + row * 9 + 9, INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));

    // Draw the filter slots
    for (int i = 0; i < tmpFilterInventory.getSizeInventory(); ++i) {
      filterSlots.add((FilterSlot) addSlot(new FilterSlot(tmpFilterInventory, i, 5 + (i % 5) * SLOT_SIZE, INVENTORY_Y + (i >= 5 ? SLOT_SIZE : 0))));
      filterSlots.get(i).setEnabled(false);
    }

    // The first 2 bytes
    this.trackInt(new IntReferenceHolder() {
      @Override
      public int get() {
        return routerTE.getEnergyStorage().getEnergyStored() & 0x0000FFFF;
      }

      @Override
      public void set(int amount) {
        routerTE.getEnergyStorage().setEnergyStored((0xFFFF0000 & routerTE.getEnergyStorage().getEnergyStored()) | (amount & 0x0000FFFF), false);
      }
    });
    // The last 2 bytes
    this.trackInt(new IntReferenceHolder() {
      @Override
      public int get() {
        return routerTE.getEnergyStorage().getEnergyStored() >>> 16;
      }

      @Override
      public void set(int amount) {
        routerTE.getEnergyStorage().setEnergyStored((amount << 16) | (0x0000FFFF & routerTE.getEnergyStorage().getEnergyStored()), true);
      }
    });

    this.trackedMappingsReference = new MappingsReferenceHolder(() -> {
      return routerTE.getMappings();
    }, (INBT mappingsNBT) -> {
      routerTE.readMappings((CompoundNBT) mappingsNBT);
    });
  }

  public void setFilterSlotContents(Inventory filter) {
    for (int i = 0; i < tmpFilterInventory.getSizeInventory(); ++i) {
      filterSlots.get(i).setEnabled(true);
      tmpFilterInventory.setInventorySlotContents(i, filter.getStackInSlot(i));
    }
  }

  @Override
  public void addListener(IContainerListener listener) {
    super.addListener(listener);
    if (listener instanceof ServerPlayerEntity)
      listeners.add((ServerPlayerEntity) listener);
  }

  @Override
  public void removeListener(IContainerListener listener) {
    super.removeListener(listener);
    if (listener instanceof ServerPlayerEntity)
      listeners.remove(listener);
  }

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();

    if (trackedMappingsReference.isDirty()) {
      SetMappingsPacket packet = new SetMappingsPacket(routerTE.getPos(), Mapping.toNBT(trackedMappingsReference.get.get(), new CompoundNBT()), windowId);
      for (ServerPlayerEntity containerListener : listeners) {
        PacketHander.INSTANCE.sendTo(packet, containerListener.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
      }
    }

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

  public void disableFilterSlots() {
    for (FilterSlot filterSlot : filterSlots) {
      filterSlot.setEnabled(false);
      filterSlot.putStack(ItemStack.EMPTY);
    }
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
    return ContainerTypes.ROUTER_CONTAINER;
  }

  public RouterTE getRouterTE() {
    return routerTE;
  }

  public ArrayList<FilterSlot> getFilterSlots() {
    return filterSlots;
  }

  public Inventory getTmpFilterInventory() {
    return tmpFilterInventory;
  }

  public MappingsReferenceHolder getTrackedMappingsReference() {
    return trackedMappingsReference;
  }

  /**
   * Copies the slot contents to an inventory.
   * @param inventory The inventory to copy to.
   */
  public void copySlotContents(Inventory inventory) {
    for (int i = 0; i < routerTE.getTier().filterSize; ++i)
      inventory.setInventorySlotContents(i, tmpFilterInventory.getStackInSlot(i));
  }

}
