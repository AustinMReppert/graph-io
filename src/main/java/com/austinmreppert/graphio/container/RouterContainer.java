package com.austinmreppert.graphio.container;

import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import com.austinmreppert.graphio.client.gui.FilterSlot;
import com.austinmreppert.graphio.client.gui.RouterScreen;
import com.austinmreppert.graphio.data.RedstoneMode;
import com.austinmreppert.graphio.data.mappings.Mapping;
import com.austinmreppert.graphio.network.PacketHandler;
import com.austinmreppert.graphio.network.SetRouterBEMappingsPacket;
import com.austinmreppert.graphio.network.SetRouterRedstoneMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

/**
 * Synchronizes data between client and server for the {@link com.austinmreppert.graphio.client.gui.RouterScreen}.
 */
public class RouterContainer extends AbstractContainerMenu {

  private ArrayList<Mapping> mappings;

  private final RouterBlockEntity routerBlockEntity;
  private final ArrayList<FilterSlot> filterSlots;
  private final SimpleContainer tmpFilterInventory;

  private static final int HOTBAR_X = 108;
  private static final int HOTBAR_Y = 232;
  private static final int INVENTORY_X = 108;
  private static final int INVENTORY_Y = 174;
  private static final int SLOT_SIZE = 18;
  private ServerPlayer listener;

  /**
   * Creates the router's container client side.
   * @param windowId The id of the window.
   * @param inv The player's inventory.
   * @param data A {@link FriendlyByteBuf} containing the block pos of the router.
   */
  public RouterContainer(final int windowId, final Inventory inv, final FriendlyByteBuf data) {
    this(windowId, inv, (RouterBlockEntity) inv.player.level.getBlockEntity(data.readBlockPos()));
  }

  public RouterContainer(final int windowId, final Inventory inv, final RouterBlockEntity routerBlockEntity) {
    super(ContainerTypes.ROUTER_CONTAINER.get(), windowId);

    if(inv.player instanceof ServerPlayer serverPlayer)
      listener = serverPlayer;

    this.routerBlockEntity = routerBlockEntity;
    this.mappings = routerBlockEntity.getMappings();

    addDataSlot(new DataSlot() {
      @Override
      public int get() {
        return routerBlockEntity.redstoneMode.ordinal();
      }

      @Override
      public void set(final int ordinal) {
        routerBlockEntity.redstoneMode = RedstoneMode.valueOf(ordinal);
        routerBlockEntity.setChanged();
      }
    });

    tmpFilterInventory = new SimpleContainer(routerBlockEntity.getTier().filterSize);
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
        return routerBlockEntity.getEnergyStorage().getEnergyStored() & 0x0000FFFF;
      }

      @Override
      public void set(final int amount) {
        routerBlockEntity.getEnergyStorage().setEnergyStored((0xFFFF0000 & routerBlockEntity.getEnergyStorage().getEnergyStored()) | (amount & 0x0000FFFF), false);
      }
    });
    // The last 2 bytes
    this.addDataSlot(new DataSlot() {
      @Override
      public int get() {
        return routerBlockEntity.getEnergyStorage().getEnergyStored() >>> 16;
      }

      @Override
      public void set(final int amount) {
        routerBlockEntity.getEnergyStorage().setEnergyStored((amount << 16) | (0x0000FFFF & routerBlockEntity.getEnergyStorage().getEnergyStored()), true);
      }
    });
  }

  /**
   * Sets the contents of the filter.
   *
   * @param filter The filter items.
   */
  public void setFilterSlotContents(final SimpleContainer filter) {
    for (int i = 0; i < tmpFilterInventory.getContainerSize(); ++i) {
      filterSlots.get(i).setEnabled(true);
      tmpFilterInventory.setItem(i, filter.getItem(i));
    }
  }

  /***
   * Get whether the slot can take ownership of items.
   * @param slot The slot.
   * @return Whether the slot can take ownership of items.
   */
  @Override
  @ParametersAreNonnullByDefault
  public boolean canDragTo(final Slot slot) {
    if (slot instanceof FilterSlot) return false;
    return super.canDragTo(slot);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void clicked(final int slotId, final int dragType, final ClickType clickType, final Player player) {
    final Slot slot = slotId < 0 ? null : getSlot(slotId);
    if (slot instanceof FilterSlot) {
      if (clickType == ClickType.PICKUP && getCarried().isEmpty())
        slot.set(ItemStack.EMPTY);
      else
        slot.set(getCarried().isEmpty() ? ItemStack.EMPTY : getCarried().copy());
    } else
      super.clicked(slotId, dragType, clickType, player);
  }

  /**
   * Disables the filter slots.
   */
  public void disableFilterSlots() {
    for (FilterSlot filterSlot : filterSlots) {
      filterSlot.setEnabled(false);
      filterSlot.set(ItemStack.EMPTY);
    }
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public ItemStack quickMoveStack(final Player player, final int index) {
    return ItemStack.EMPTY;
  }

  @Override
  @ParametersAreNonnullByDefault
  public void removed(final Player player) {
    super.removed(player);
  }

  @Override
  @ParametersAreNonnullByDefault
  public boolean stillValid(final Player player) {
    return routerBlockEntity.stillValid(player);
  }

  /**
   * Gets the {@link MenuType} of this container.
   *
   * @return The {@link MenuType} of this container.
   */
  @Override
  @Nonnull
  public MenuType<?> getType() {
    return ContainerTypes.ROUTER_CONTAINER.get();
  }

  /**
   * Gets the {@link RouterBlockEntity} that is associated with this container.
   *
   * @return The {@link RouterBlockEntity} that is associated with this container.
   */
  public RouterBlockEntity getRouterBlockEntity() {
    return routerBlockEntity;
  }

  /**
   * Gets the filter slots.
   *
   * @return The filter slots.
   */
  public ArrayList<FilterSlot> getFilterSlots() {
    return filterSlots;
  }

  /**
   * Gets the temporary inventory used to store filtered items.
   *
   * @return The temporary inventory used to store filtered items.
   */
  public SimpleContainer getTmpFilterInventory() {
    return tmpFilterInventory;
  }

  /**
   * Gets the container's current list of mappings.
   *
   * @return The container's current list of mappings.
   */
  public ArrayList<Mapping> getMappings() {
    return mappings;
  }

  /**
   * Copies the slot contents to an inventory.
   *
   * @param inventory The inventory to copy to.
   */
  public void copySlotContents(SimpleContainer inventory) {
    for (int i = 0; i < routerBlockEntity.getTier().filterSize; ++i)
      inventory.setItem(i, tmpFilterInventory.getItem(i));
  }

  /**
   * Sets the router's mappings on the server.
   */
  public void setServerMappings() {
    PacketHandler.INSTANCE.sendToServer(new SetRouterBEMappingsPacket(routerBlockEntity.getBlockPos(), Mapping.write(mappings), containerId));
  }

  /**
   * Detects and sends changes. Non-slots are checked for differences by comparing the block entity and container fields.
   */
  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    if (listener != null && routerBlockEntity.getMappings() != mappings) {
      mappings = routerBlockEntity.getMappings();
      PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> listener), new SetRouterBEMappingsPacket(routerBlockEntity.getBlockPos(), Mapping.write(mappings), containerId));
    }
  }

  /**
   * Sets the containers mappings on the client.
   * @param routerBENBT The nbt data of the mappings.
   * @param windowID This container's id.
   */
  public void setClientMappings(final CompoundTag routerBENBT, final int windowID) {
    mappings = Mapping.read(routerBENBT, routerBlockEntity.getIdentifiers(), routerBlockEntity.getTier());
    if (listener == null) {
      final Screen currentTmp = Minecraft.getInstance().screen;
      if (currentTmp instanceof RouterScreen current && Minecraft.getInstance().player.containerMenu.containerId == windowID)
        current.update();
    }
  }

  /**
   * Synchronizes the client router's redstone mode to the server.
   * @param redstoneMode The new redstone mode.
   */
  public void setSeverRedstoneMode(final RedstoneMode redstoneMode) {
    PacketHandler.INSTANCE.sendToServer(new SetRouterRedstoneMode(routerBlockEntity.getBlockPos(), redstoneMode, containerId));
  }

  /**
   * Sends all container data to the remote container. Mainly used when opening the container.
   */
  @Override
  public void sendAllDataToRemote() {
    super.sendAllDataToRemote();
    if (listener != null && routerBlockEntity.getMappings() != null) {
      mappings = routerBlockEntity.getMappings();
      PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> listener),
          new SetRouterBEMappingsPacket(routerBlockEntity.getBlockPos(), Mapping.write(mappings), containerId));
    }
  }

}