package com.austinmreppert.graphio.blockentity;

import com.austinmreppert.graphio.Config;
import com.austinmreppert.graphio.block.Blocks;
import com.austinmreppert.graphio.capabilities.Capabilities;
import com.austinmreppert.graphio.capabilities.DynamicEnergyStorage;
import com.austinmreppert.graphio.capabilities.IIdentifierCapability;
import com.austinmreppert.graphio.client.gui.RouterScreen;
import com.austinmreppert.graphio.container.RouterContainer;
import com.austinmreppert.graphio.container.RouterStorageContainer;
import com.austinmreppert.graphio.data.RedstoneMode;
import com.austinmreppert.graphio.data.mappings.Mapping;
import com.austinmreppert.graphio.data.mappings.NodeInfo;
import com.austinmreppert.graphio.data.tiers.BaseTier;
import com.austinmreppert.graphio.data.tiers.RouterTier;
import com.austinmreppert.graphio.item.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RouterBlockEntity extends RandomizableContainerBlockEntity implements MenuProvider {

  private final Random random;
  private final HashMap<String, IIdentifierCapability> identifiers;
  private final DynamicEnergyStorage energyStorage;
  private final LazyOptional<IEnergyStorage> energyCapabilityLO;
  private ArrayList<Mapping> mappings;
  private NonNullList<ItemStack> inventory;
  private int ticks;
  private RouterTier tier;
  protected Block tieredRouter;
  private int lastTick;
  public static int ITEM_TRANSFER_COST;
  public static float ENERGY_TRANSFER_COST;
  public static int FLUID_TRANSFER_COST;
  private static int maxMappings;
  private int containerSize;
  public RedstoneMode redstoneMode;

  public RouterBlockEntity(final BlockPos pos, final BlockState state) {
    super(BlockEntityTypes.ROUTER, pos, state);
    containerSize = 3;
    identifiers = new HashMap<>();
    random = new Random(System.currentTimeMillis());
    mappings = new ArrayList<>();
    ticks = 0;
    energyStorage = new DynamicEnergyStorage(0);
    energyCapabilityLO = LazyOptional.of(() -> energyStorage);
    redstoneMode = RedstoneMode.IGNORED;
  }

  public RouterBlockEntity(final BaseTier tier, final BlockPos pos, final BlockState state) {
    this(pos, state);
    setTier(tier);
  }

  /**
   * Determines whether the router should update or not. Used only for inputting energy.
   *
   * @return Whether the router should update or not.
   */
  public boolean shouldUpdate() {
    if (ticks - lastTick >= tier.updateDelay) {
      lastTick = ticks;
      return true;
    }
    return false;
  }

  // TODO: Refactor

  /**
   * Performs updates for the Router.
   */
  public void serverTick() {
    ++ticks;

    if (this.level == null)
      return;

    final var receivedEnergy = new AtomicInteger();
    if (shouldUpdate()) {
      for (final var direction : Direction.values()) {
        if (energyStorage.getEnergyStored() == energyStorage.getMaxEnergyStored())
          break;
        final var blockEntity = this.level.getBlockEntity(getBlockPos().relative(direction));
        if (blockEntity != null) {
          blockEntity.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).ifPresent(cap -> {
            final int simulatedReceived = energyStorage.receiveEnergy(cap.extractEnergy(energyStorage.getMaxReceive() - receivedEnergy.get(), true), true);
            receivedEnergy.addAndGet(energyStorage.receiveEnergy(cap.extractEnergy(simulatedReceived, false), false));
            blockEntity.setChanged();
            setChanged();
          });
        }
      }
    }

    if (mappings.size() > maxMappings)
      return;

    final var powered = level.hasNeighborSignal(this.getBlockPos());
    if ((redstoneMode == RedstoneMode.ACTIVE && !powered) || (redstoneMode == RedstoneMode.INACTIVE && powered)) {
      return;
    }

    for (final var mapping : mappings) {

      if (!mapping.isValid() || !mapping.shouldUpdate(ticks))
        continue;

      SimpleContainer filterInventory = mapping.getFilterInventory();
      final var filterScheme = mapping.getFilterScheme();
      if (mapping.getInputs().isEmpty() || mapping.getOutputs().isEmpty()) continue;

      final NodeInfo inputNodeInfo = mapping.getInputs().get(mapping.currentInputIndex);
      final NodeInfo outputNodeInfo = mapping.getOutputs().get(mapping.currentOutputIndex);

      if (identifiers.get(inputNodeInfo.getIdentifier()) == null || identifiers.get(outputNodeInfo.getIdentifier()) == null)
        continue;

      final BlockPos inputPos = identifiers.get(inputNodeInfo.getIdentifier()).getBlockPos();
      final BlockPos outputPos = identifiers.get(outputNodeInfo.getIdentifier()).getBlockPos();
      if (inputPos == null || outputPos == null || (inputPos.equals(outputPos) && inputNodeInfo.getFace() == outputNodeInfo.getFace()))
        continue;

      final ResourceKey<Level> inputLevelName = identifiers.get(inputNodeInfo.getIdentifier()).getLevel();
      final ResourceKey<Level> outputLevelName = identifiers.get(outputNodeInfo.getIdentifier()).getLevel();

      if (inputLevelName == null || outputLevelName == null || level.getServer() == null)
        continue;

      final var inputLevel = level.getServer().getLevel(inputLevelName);
      final var outputLevel = level.getServer().getLevel(outputLevelName);

      if (inputLevel == null || outputLevel == null)
        continue;

      if (!inputLevel.isLoaded(inputPos) || !outputLevel.isLoaded(outputPos))
        continue;

      final var inputBlockEntity = inputLevel.getBlockEntity(inputPos);
      final var outputBlockEntity = outputLevel.getBlockEntity(outputPos);
      if (inputBlockEntity == null || outputBlockEntity == null) return;

      inputBlockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputItemHandler ->
          outputBlockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputItemHandler -> {
            int transferredItems = 0;
            for (int inputSlotIndex = 0; inputSlotIndex < inputItemHandler.getSlots(); ++inputSlotIndex) {
              final ItemStack inputStack = inputItemHandler.getStackInSlot(inputSlotIndex);
              boolean filtered = filterScheme != Mapping.FilterScheme.BLOCK_LIST;
              for (int i = 0; i < filterInventory.getContainerSize(); ++i) {
                if (filterInventory.getItem(i).getItem() == inputStack.getItem()) {
                  filtered = !filtered;
                  break;
                }
              }
              if (filtered) continue;
              for (int outputSlotIndex = 0; outputSlotIndex < outputItemHandler.getSlots(); ++outputSlotIndex) {
                if (transferredItems >= mapping.getItemsPerUpdate()) return;
                final ItemStack outputStack = outputItemHandler.getStackInSlot(outputSlotIndex);
                if (outputItemHandler.isItemValid(outputSlotIndex, inputStack) && !inputStack.isEmpty() && outputStack.isEmpty() || (!inputStack.isEmpty() && inputStack.getItem() == outputStack.getItem() && outputStack.getCount() < outputStack.getMaxStackSize())) {
                  final ItemStack simulatedExtractedIS = inputItemHandler.extractItem(
                      inputSlotIndex,
                      Mth.clamp(inputStack.getCount(), 0,
                          Math.min(mapping.getItemsPerUpdate() - transferredItems, energyStorage.getEnergyStored() / ITEM_TRANSFER_COST)),
                      true);
                  final ItemStack simulatedInsertedLeftoversIS = outputItemHandler.insertItem(outputSlotIndex, simulatedExtractedIS, true);
                  final ItemStack extractedIS = inputItemHandler.extractItem(inputSlotIndex, simulatedExtractedIS.getCount() - simulatedInsertedLeftoversIS.getCount(), false);
                  final ItemStack insertedIS = outputItemHandler.insertItem(outputSlotIndex, extractedIS, false);
                  final int transferredItemsCount = extractedIS.getCount() - insertedIS.getCount();
                  transferredItems += transferredItemsCount;
                  inputBlockEntity.setChanged();
                  outputBlockEntity.setChanged();

                  energyStorage.setEnergyStored(energyStorage.getEnergyStored() - ITEM_TRANSFER_COST * transferredItemsCount, false);
                  setChanged();
                }
              }
            }
          }));

      inputBlockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputFluidHandler ->
          outputBlockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputFluidHandler -> {
            int transferredFluids = 0;
            for (int inputSlotIndex = 0; inputSlotIndex < inputFluidHandler.getTanks(); ++inputSlotIndex) {
              final FluidStack inputStack = inputFluidHandler.getFluidInTank(inputSlotIndex);
              boolean filtered = filterScheme != Mapping.FilterScheme.BLOCK_LIST;
              for (int i = 0; i < filterInventory.getContainerSize(); ++i) {
                if (inputStack.isFluidEqual(filterInventory.getItem(i))) {
                  filtered = !filtered;
                  break;
                }
              }
              if (filtered) continue;
              for (int outputSlotIndex = 0; outputSlotIndex < outputFluidHandler.getTanks(); ++outputSlotIndex) {
                if (transferredFluids >= mapping.getFluidPerUpdate()) return;
                final FluidStack outputStack = outputFluidHandler.getFluidInTank(outputSlotIndex);
                if (outputFluidHandler.isFluidValid(outputSlotIndex, inputStack)) {
                  final FluidStack simulatedDrainedFS = inputFluidHandler.drain(
                      Mth.clamp(inputStack.getAmount(), 0,
                          Math.min(mapping.getFluidPerUpdate() - transferredFluids, energyStorage.getEnergyStored() / FLUID_TRANSFER_COST)),
                      IFluidHandler.FluidAction.SIMULATE);
                  final int simulatedFilled = outputFluidHandler.fill(simulatedDrainedFS, IFluidHandler.FluidAction.SIMULATE);
                  final FluidStack drainedFS = inputFluidHandler.drain(simulatedFilled, IFluidHandler.FluidAction.EXECUTE);
                  final int transferredFluidsCount = outputFluidHandler.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);

                  transferredFluids += transferredFluidsCount;
                  inputBlockEntity.setChanged();
                  outputBlockEntity.setChanged();

                  energyStorage.setEnergyStored(energyStorage.getEnergyStored() - FLUID_TRANSFER_COST * transferredFluidsCount, false);
                  setChanged();

                  break;
                }
              }
            }
          }));

      inputBlockEntity.getCapability(CapabilityEnergy.ENERGY, inputNodeInfo.getFace()).ifPresent(inputEnergyHandler ->
          outputBlockEntity.getCapability(CapabilityEnergy.ENERGY, outputNodeInfo.getFace()).ifPresent(outputEnergyHandler -> {
            int transferredEnergy = 0;
            if (inputEnergyHandler.canExtract() && outputEnergyHandler.canReceive()) {
              final int simulatedExtracted = inputEnergyHandler.extractEnergy(
                  Mth.clamp(inputEnergyHandler.getEnergyStored(), 0, mapping.getEnergyPerUpdate() - transferredEnergy),
                  true);
              final int simulatedReceived = outputEnergyHandler.receiveEnergy(simulatedExtracted, true);
              final int extracted = inputEnergyHandler.extractEnergy(simulatedReceived, false);
              final var transferredEnergyCount = outputEnergyHandler.receiveEnergy((int) (extracted*ENERGY_TRANSFER_COST), false);
              transferredEnergy += transferredEnergyCount;

              inputBlockEntity.setChanged();
              outputBlockEntity.setChanged();
            }
          }));


      switch (mapping.getDistributionScheme()) {
        case CYCLIC -> {
          mapping.currentInputIndex = (mapping.currentInputIndex + 1) % Math.max(mapping.getInputs().size(), 1);
          mapping.currentOutputIndex = (mapping.currentOutputIndex + 1) % Math.max(mapping.getOutputs().size(), 1);
        }
        case NATURAL -> {
          ++mapping.currentOutputIndex;
          if (mapping.currentOutputIndex >= mapping.getOutputs().size()) {
            mapping.currentOutputIndex = 0;
            ++mapping.currentInputIndex;
            if (mapping.currentInputIndex >= mapping.getInputs().size())
              mapping.currentInputIndex = 0;
          }
        }
        case RANDOM -> {
          mapping.currentInputIndex = random.nextInt(mapping.getInputs().size());
          mapping.currentOutputIndex = random.nextInt(mapping.getOutputs().size());
        }
      }
      setChanged();
    }
  }

  /**
   * @param windowID  The ID for the new menu.
   * @param inventory The player's inventory.
   * @param player    The player that opened the container.
   * @return A {@link RouterStorageContainer} if the player is crouching or a {@link RouterContainer}.
   */
  @Override
  @ParametersAreNonnullByDefault
  public AbstractContainerMenu createMenu(final int windowID, final Inventory inventory, final Player player) {
    return createMenu(windowID, inventory);
  }

  /**
   * Creates an {@link AbstractContainerMenu} instance.
   *
   * @param windowID  The ID for the new menu.
   * @param inventory The player's inventory.
   * @return A {@link RouterStorageContainer} if the player is crouching or a {@link RouterContainer}.
   */
  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  protected AbstractContainerMenu createMenu(final int windowID, final Inventory inventory) {
    if (inventory.player.isCrouching())
      return new RouterStorageContainer(windowID, inventory, this);
    else
      return new RouterContainer(windowID, inventory, this);
  }

  /**
   * Saves the router's data into a {@link CompoundTag}  when the {@link net.minecraft.world.level.chunk.LevelChunk} containing the BE is unloaded.
   *
   * @param nbtOut The {@link CompoundTag} to save to.
   * @return The updated {@code nbtOut}.
   */
  @Override
  @Nonnull
  public void saveAdditional(final CompoundTag nbtOut) {
    super.saveAdditional(nbtOut);
    nbtOut.putInt("tier", tier.baseTier.ordinal());
    nbtOut.put("energyStorage", energyStorage.serializeNBT());
    nbtOut.putInt("redstoneMode", redstoneMode.ordinal());
    writeMappingsNBT(nbtOut);
    writeInventoryNBT(nbtOut);
  }

  /**
   * Writes the router's inventory to a {@link CompoundTag}.
   *
   * @param compound The {@link CompoundTag} used to store the inventory data.
   */
  public void writeInventoryNBT(final CompoundTag compound) {
    if (!this.trySaveLootTable(compound))
      ContainerHelper.saveAllItems(compound, inventory);
  }

  /**
   * Writes the router's mappings to a nbt tag.
   *
   * @param compound The nbt tag to write to.
   */
  private void writeMappingsNBT(final CompoundTag compound) {
    Mapping.write(mappings, compound);
  }

  /**
   * Loads NBT data into the {@link RouterBlockEntity} when the {@link net.minecraft.world.level.chunk.LevelChunk} containing the BE is loaded.
   *
   * @param nbt The {@link CompoundTag} to read from.
   */
  @Override
  @ParametersAreNonnullByDefault
  public void load(final CompoundTag nbt) {
    super.load(nbt);
    setTier(BaseTier.valueOf(nbt.getInt("tier")));
    final Tag energyStorageNBT = nbt.get("energyStorage");
    redstoneMode = RedstoneMode.valueOf(nbt.getInt("redstoneMode"));
    if (energyStorageNBT != null) {
      energyStorage.deserializeNBT(energyStorageNBT);
    }
    readInventory(nbt);
    mappings = Mapping.read(nbt, identifiers, tier);
  }

  /**
   * Reads the inventory in from nbt data.
   *
   * @param nbt The nbt data to read from.
   */
  public void readInventory(CompoundTag nbt) {
    setItems(NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY));
    if (!this.tryLoadLootTable(nbt))
      ContainerHelper.loadAllItems(nbt, inventory);
    identifiers.clear();
    for (final ItemStack is : inventory)
      cacheIfIdentifier(is);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    energyCapabilityLO.invalidate();
  }

  @Override
  @Nonnull
  public ItemStack removeItemNoUpdate(int index) {
    identifiers.clear();
    for (ItemStack is : inventory)
      cacheIfIdentifier(is);
    return super.removeItemNoUpdate(index);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void setItem(int index, ItemStack itemStack) {
    identifiers.clear();
    for (final ItemStack is : inventory)
      cacheIfIdentifier(is);
    cacheIfIdentifier(itemStack);
    super.setItem(index, itemStack);
  }

  /**
   * If the item has identifier capabilities, cache the capability information.
   *
   * @param is The item stack to check.
   */
  private void cacheIfIdentifier(final ItemStack is) {
    if (is.getItem() == Items.IDENTIFIER)
      is.getCapability(Capabilities.IDENTIFIER_CAPABILITY, null).ifPresent(identifierCapability ->
          identifiers.put(is.getHoverName().getContents(), identifierCapability));
  }

  /**
   * Gets the corresponding capability.
   *
   * @param capability The capability holder.
   * @param <T>        The type of capability to get.
   * @return An {@link LazyOptional<T>} that will contain an instance of the capability, if it exists.
   */
  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> capability) {
    return CapabilityEnergy.ENERGY.orEmpty(capability, energyCapabilityLO);
  }

  /**
   * Reads mappings in from NBT data. If the client has the GUI open, then update the GUI.
   *
   * @param nbt The {@link CompoundTag} data to read from.
   */
  public void readMappings(CompoundTag nbt) {
    mappings = Mapping.read(nbt, identifiers, tier);
    if (level != null && level.isClientSide()) {
      final Screen currentTmp = Minecraft.getInstance().screen;
      if (currentTmp instanceof RouterScreen current) {
        current.update();
      }
    }
  }

  /**
   * Gets a list of the router's mappings.
   *
   * @return A list of the router's mappings.
   */
  public ArrayList<Mapping> getMappings() {
    return mappings;
  }

  /**
   * Gets the inventory of this router.
   *
   * @return The inventory of this router.
   */
  @Override
  @Nonnull
  protected NonNullList<ItemStack> getItems() {
    return inventory;
  }

  /**
   * Sets the items in the router's inventory.
   *
   * @param itemsIn A list of items in the router's inventory.
   */
  @Override
  protected void setItems(NonNullList<ItemStack> itemsIn) {
    identifiers.clear();
    for (ItemStack is : itemsIn)
      cacheIfIdentifier(is);
    this.inventory = itemsIn;
  }

  @Override
  @Nonnull
  public Component getDisplayName() {
    return tieredRouter.getName();
  }

  @Override
  @Nonnull
  protected Component getDefaultName() {
    return getDisplayName();
  }

  /**
   * Gets the {@link RouterTier} of the router.
   *
   * @return The {@link RouterTier} of the router.
   */
  public RouterTier getTier() {
    return tier;
  }

  /**
   * Sets the router's tier.
   *
   * @param baseTier The tier to be set to.
   */
  public void setTier(@Nonnull BaseTier baseTier) {
    if(tier != null && baseTier == tier.baseTier)
      return;
    this.tier = new RouterTier(baseTier);
    if (tier.baseTier == BaseTier.BASIC) {
      tieredRouter = Blocks.BASIC_ROUTER;
      containerSize = 3;
      energyStorage.setMaxReceive(Config.SERVER_CONFIG.BASIC_ROUTER_ENERGY_INPUT_PER_UPDATE.get());
      energyStorage.setCapacity(Config.SERVER_CONFIG.BASIC_ROUTER_ENERGY_CAPACITY.get());
      maxMappings = Config.SERVER_CONFIG.BASIC_ROUTER_NUM_MAPPINGS.get();
    } else if (tier.baseTier == BaseTier.ADVANCED) {
      tieredRouter = Blocks.ADVANCED_ROUTER;
      containerSize = 6;
      energyStorage.setMaxReceive(Config.SERVER_CONFIG.ADVANCED_ROUTER_ENERGY_INPUT_PER_UPDATE.get());
      energyStorage.setCapacity(Config.SERVER_CONFIG.ADVANCED_ROUTER_ENERGY_CAPACITY.get());
      maxMappings = Config.SERVER_CONFIG.ADVANCED_ROUTER_NUM_MAPPINGS.get();
    } else if (tier.baseTier == BaseTier.ELITE) {
      tieredRouter = Blocks.ELITE_ROUTER;
      containerSize = 9;
      energyStorage.setMaxReceive(Config.SERVER_CONFIG.ELITE_ROUTER_ENERGY_INPUT_PER_UPDATE.get());
      energyStorage.setCapacity(Config.SERVER_CONFIG.ELITE_ROUTER_ENERGY_CAPACITY.get());
      maxMappings = Config.SERVER_CONFIG.ELITE_ROUTER_NUM_MAPPINGS.get();
    } else if (tier.baseTier == BaseTier.ULTIMATE) {
      tieredRouter = Blocks.ULTIMATE_ROUTER;
      containerSize = 12;
      energyStorage.setMaxReceive(Config.SERVER_CONFIG.ULTIMATE_ROUTER_ENERGY_INPUT_PER_UPDATE.get());
      energyStorage.setCapacity(Config.SERVER_CONFIG.ULTIMATE_ROUTER_ENERGY_CAPACITY.get());
      maxMappings = Config.SERVER_CONFIG.ULTIMATE_ROUTER_NUM_MAPPINGS.get();
    } else {
      throw new IllegalStateException("Router has no tier.");
    }
    inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
  }

  /**
   * Gets the {@link DynamicEnergyStorage} capability for this router.
   *
   * @return Gets the {@link DynamicEnergyStorage} capability for this router.
   */
  public DynamicEnergyStorage getEnergyStorage() {
    return energyStorage;
  }

  /**
   * Gets the size in slots of the router's inventory.
   *
   * @return The size in slots of the router's inventory.
   */
  @Override
  public int getContainerSize() {
    return containerSize;
  }

  /**
   * Gets the maximum number of mappings this router supports.
   *
   * @return the maximum number of mappings this router supports.
   */
  public static int getMaxMappings() {
    return maxMappings;
  }

  public HashMap<String, IIdentifierCapability> getIdentifiers() {
    return identifiers;
  }

}
