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
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

/**
 * Synchronizes data between client and server for the {@link com.austinmreppert.graphio.client.gui.RouterScreen}.
 */
public class RouterContainer extends AbstractContainerMenu {

  private ArrayList<Mapping> mappings;
  private final DataSlot redstoneMode;

  private final RouterBlockEntity routerBlockEntity;
  private final ArrayList<FilterSlot> filterSlots;
  private final SimpleContainer tmpFilterInventory;

  private static final int HOTBAR_X = 108;
  private static final int HOTBAR_Y = 232;
  private static final int INVENTORY_X = 108;
  private static final int INVENTORY_Y = 174;
  private static final int SLOT_SIZE = 18;
  private ServerPlayer listener;

  public RouterContainer(final int windowId, final Inventory inv, final FriendlyByteBuf data) {
    this(windowId, inv, (RouterBlockEntity) inv.player.level.getBlockEntity(data.readBlockPos()));
    setMappings(data.readNbt());
  }

  public RouterContainer(final int windowId, final Inventory inv, final RouterBlockEntity routerBlockEntity) {
    super(ContainerTypes.ROUTER_CONTAINER, windowId);

    if(inv.player instanceof ServerPlayer serverPlayer)
      listener = serverPlayer;

    this.routerBlockEntity = routerBlockEntity;

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
      public void set(int amount) {
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
      public void set(int amount) {
        routerBlockEntity.getEnergyStorage().setEnergyStored((amount << 16) | (0x0000FFFF & routerBlockEntity.getEnergyStorage().getEnergyStored()), true);
      }
    });

    redstoneMode = this.addDataSlot(new DataSlot() {
      @Override
      public int get() {
        return routerBlockEntity.redstoneMode.ordinal();
      }

      @Override
      public void set(final int ordinal) {
        routerBlockEntity.redstoneMode = RedstoneMode.valueOf(ordinal);
        final var nbt = new CompoundTag();
        nbt.putInt("redstoneMode", ordinal);
        PacketHandler.INSTANCE.sendToServer(new SetRouterRedstoneMode(routerBlockEntity.getBlockPos(), nbt, windowId));
      }
    });

    this.mappings = routerBlockEntity.getMappings();
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
    return true;
  }

  /**
   * Gets the {@link MenuType} of this container.
   *
   * @return The {@link MenuType} of this container.
   */
  @Override
  @Nonnull
  public MenuType<?> getType() {
    return ContainerTypes.ROUTER_CONTAINER;
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
   * Gets the redstone mode of the router.
   * @return The redstone mode of the router.
   */
  public DataSlot getRedstoneMode() {
    return redstoneMode;
  }

  /**
   * Sends mapping changes to the server.
   */
  public void updateMappings() {
    PacketHandler.INSTANCE.sendToServer(new SetRouterBEMappingsPacket(routerBlockEntity.getBlockPos(), Mapping.write(mappings), containerId));
  }

  /**
   * Detects and sends changes.
   */
  @Override
  public void broadcastChanges() {
    super.broadcastChanges();
    if(routerBlockEntity.getMappings() != mappings) {
      mappings = routerBlockEntity.getMappings();
      if (listener != null)
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> listener), new SetRouterBEMappingsPacket(routerBlockEntity.getBlockPos(), Mapping.write(mappings), containerId));
    }
  }

  /**
   * Sets the containers mappings. If this is called client side, then the Router Screen will be updated.
   * @param routerTENBT The nbt data of the mappings.
   */
  public void setMappings(final CompoundTag routerTENBT) {
    mappings = Mapping.read(routerTENBT, routerBlockEntity.getIdentifiers(), routerBlockEntity.getTier());
    if (listener == null) {
      final Screen currentTmp = Minecraft.getInstance().screen;
      if (currentTmp instanceof RouterScreen current)
        current.update();
    }
  }

}
