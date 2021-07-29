package xyz.austinmreppert.graph_io.container;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import xyz.austinmreppert.graph_io.client.gui.FilterSlot;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;
import xyz.austinmreppert.graph_io.network.PacketHander;
import xyz.austinmreppert.graph_io.network.SetMappingsPacket;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public class RouterContainer extends AbstractContainerMenu {

  private final List<ServerPlayer> listeners;
  private final MappingsReferenceHolder trackedMappingsReference;

  private final RouterTE routerTE;
  private final ArrayList<FilterSlot> filterSlots;
  private final SimpleContainer tmpFilterInventory;

  private final int HOTBAR_X = 108;
  private final int HOTBAR_Y = 232;
  private final int INVENTORY_X = 108;
  private final int INVENTORY_Y = 174;
  private final int SLOT_SIZE = 18;

  public RouterContainer(int windowId, Inventory inv, FriendlyByteBuf data) {
    this(windowId, inv, (RouterTE) inv.player.level.getBlockEntity(data.readBlockPos()));
    trackedMappingsReference.set.accept(data.readNbt());
  }

  public RouterContainer(int windowId, Inventory inv, RouterTE routerTE) {
    super(ContainerTypes.ROUTER_CONTAINER, windowId);

    listeners = Lists.newArrayList();

    this.routerTE = routerTE;

    tmpFilterInventory = new SimpleContainer(routerTE.getTier().filterSize);
    filterSlots = new ArrayList<>();

    // Draw the hotbar
    for (int column = 0; column < 9; ++column)
      this.addSlot(new Slot(inv, column, HOTBAR_X + column * SLOT_SIZE, HOTBAR_Y));

    // Draw the player inventory
    for (int row = 0; row < 3; ++row)
      for (int column = 0; column < 9; ++column)
        this.addSlot(new Slot(inv, column + row * 9 + 9, INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));

    // Draw the filter slots
    for (int i = 0; i < tmpFilterInventory.getContainerSize(); ++i) {
      filterSlots.add((FilterSlot) addSlot(new FilterSlot(tmpFilterInventory, i, 5 + (i % 5) * SLOT_SIZE, INVENTORY_Y + (i >= 5 ? SLOT_SIZE : 0))));
      filterSlots.get(i).setEnabled(false);
    }

    // The first 2 bytes
    this.addDataSlot(new DataSlot() {
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
    this.addDataSlot(new DataSlot() {
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
    }, (Tag mappingsNBT) -> {
      routerTE.readMappings((CompoundTag) mappingsNBT);
    });
  }

  public void setFilterSlotContents(SimpleContainer filter) {
    for (int i = 0; i < tmpFilterInventory.getContainerSize(); ++i) {
      filterSlots.get(i).setEnabled(true);
      tmpFilterInventory.setItem(i, filter.getItem(i));
    }
  }

  @Override
  public void addSlotListener(ContainerListener listener) {
    super.addSlotListener(listener);
    if (listener instanceof ServerPlayer)
      listeners.add((ServerPlayer) listener);
  }

  @Override
  public void removeSlotListener(ContainerListener listener) {
    super.removeSlotListener(listener);
    if (listener instanceof ServerPlayer)
      listeners.remove(listener);
  }

  @Override
  public void broadcastChanges() {
    super.broadcastChanges();

    if (trackedMappingsReference.isDirty()) {
      SetMappingsPacket packet = new SetMappingsPacket(routerTE.getBlockPos(), Mapping.write(trackedMappingsReference.get.get(), new CompoundTag()), containerId);
      for (ServerPlayer containerListener : listeners) {
        PacketHander.INSTANCE.sendTo(packet, containerListener.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
      }
    }

  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean canDragTo(Slot slot) {
    if (slot instanceof FilterSlot) return false;
    return super.canDragTo(slot);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
    Slot slot = slotId < 0 ? null : getSlot(slotId);
    if (slot instanceof FilterSlot) {
      if (clickType == ClickType.PICKUP && getCarried().isEmpty())
        slot.set(ItemStack.EMPTY);
      else
        slot.set(getCarried().isEmpty() ? ItemStack.EMPTY : getCarried().copy());
    } else
      super.clicked(slotId, dragType, clickType, player);
  }

  public void disableFilterSlots() {
    for (FilterSlot filterSlot : filterSlots) {
      filterSlot.setEnabled(false);
      filterSlot.set(ItemStack.EMPTY);
    }
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public ItemStack quickMoveStack(Player playerIn, int index) {
    return ItemStack.EMPTY;
  }

  @Override
  @ParametersAreNonnullByDefault
  public void removed(Player player) {
    super.removed(player);
  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  @Nonnull
  public MenuType<?> getType() {
    return ContainerTypes.ROUTER_CONTAINER;
  }

  public RouterTE getRouterTE() {
    return routerTE;
  }

  public ArrayList<FilterSlot> getFilterSlots() {
    return filterSlots;
  }

  public SimpleContainer getTmpFilterInventory() {
    return tmpFilterInventory;
  }

  public MappingsReferenceHolder getTrackedMappingsReference() {
    return trackedMappingsReference;
  }

  /**
   * Copies the slot contents to an inventory.
   * @param inventory The inventory to copy to.
   */
  public void copySlotContents(SimpleContainer inventory) {
    for (int i = 0; i < routerTE.getTier().filterSize; ++i)
      inventory.setItem(i, tmpFilterInventory.getItem(i));
  }

}
