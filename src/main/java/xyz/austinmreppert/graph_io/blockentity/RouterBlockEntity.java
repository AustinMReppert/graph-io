package xyz.austinmreppert.graph_io.blockentity;

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
import net.minecraft.world.level.block.entity.BlockEntity;
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
import xyz.austinmreppert.graph_io.block.Blocks;
import xyz.austinmreppert.graph_io.capabilities.Capabilities;
import xyz.austinmreppert.graph_io.capabilities.DynamicEnergyStorage;
import xyz.austinmreppert.graph_io.capabilities.IIdentifierCapability;
import xyz.austinmreppert.graph_io.client.gui.RouterScreen;
import xyz.austinmreppert.graph_io.container.RouterContainer;
import xyz.austinmreppert.graph_io.container.RouterStorageContainer;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;
import xyz.austinmreppert.graph_io.data.mappings.NodeInfo;
import xyz.austinmreppert.graph_io.data.tiers.BaseTier;
import xyz.austinmreppert.graph_io.data.tiers.RouterTier;
import xyz.austinmreppert.graph_io.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

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
  private static final int energyPerMappingTick = 1000;
  private int containerSize;

  public RouterBlockEntity(BlockPos pos, BlockState state) {
    super(BlockEntityTypes.ROUTER, pos, state);
    containerSize = 3;
    identifiers = new HashMap<>();
    random = new Random(System.currentTimeMillis());
    mappings = new ArrayList<>();
    ticks = 0;
    energyStorage = new DynamicEnergyStorage(0);
    energyCapabilityLO = LazyOptional.of(() -> energyStorage);
  }

  public RouterBlockEntity(BaseTier tier, BlockPos pos, BlockState state) {
    this(pos, state);
    setTier(tier);
  }

  public boolean shouldTick() {
    if (ticks - lastTick >= tier.minTickDelay) {
      lastTick = ticks;
      return true;
    }
    return false;
  }

  public void serverTick(Level level, BlockPos pos) {
    ++ticks;

    if (shouldTick()) {
      for (Direction direction : Direction.values()) {
        if (energyStorage.getEnergyStored() == energyStorage.getMaxEnergyStored())
          break;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity != null) {
          blockEntity.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).ifPresent(cap -> {
            int extracted = -1;
            final int simulatedReceived = energyStorage.receiveEnergy(extracted = cap.extractEnergy(tier.maxEnergyPerTick, true), true);
            energyStorage.receiveEnergy(cap.extractEnergy(simulatedReceived, false), false);
            blockEntity.setChanged();
            setChanged();
          });
        }
      }
    }

    for (Mapping mapping : mappings) {
      if (!mapping.isValid() || !mapping.shouldTick(ticks))
        continue;

      if (energyStorage.getEnergyStored() - energyPerMappingTick >= 0)
        energyStorage.extractEnergy(energyPerMappingTick, false);
      else
        continue;

      SimpleContainer filterInventory = mapping.getFilterInventory();
      Mapping.FilterScheme filterScheme = mapping.getFilterScheme();
      if (mapping.getInputs().isEmpty() || mapping.getOutputs().isEmpty()) continue;

      final NodeInfo inputNodeInfo = mapping.getInputs().get(mapping.currentInputIndex);
      final NodeInfo outputNodeInfo = mapping.getOutputs().get(mapping.currentOutputIndex);

      if(identifiers.get(inputNodeInfo.getIdentifier()) == null || identifiers.get(outputNodeInfo.getIdentifier()) == null)
        continue;

      final BlockPos inputPos = identifiers.get(inputNodeInfo.getIdentifier()).getBlockPos();
      final BlockPos outputPos = identifiers.get(outputNodeInfo.getIdentifier()).getBlockPos();
      if (inputPos == null || outputPos == null || (inputPos.equals(outputPos) && inputNodeInfo.getFace() == outputNodeInfo.getFace()))
        continue;

      final ResourceKey<Level> inputLevelName = identifiers.get(inputNodeInfo.getIdentifier()).getLevel();
      final ResourceKey<Level> outputLevelName = identifiers.get(outputNodeInfo.getIdentifier()).getLevel();

      if (inputLevelName == null || outputLevelName == null)
        continue;

      final Level inputLevel = level.getServer().getLevel(inputLevelName);
      final Level outputLevel = level.getServer().getLevel(outputLevelName);

      if (inputLevel == null || outputLevel == null)
        continue;

      if(!inputLevel.isLoaded(inputPos) || !outputLevel.isLoaded(outputPos))
        continue;

      final BlockEntity inputBlockEntity = inputLevel.getBlockEntity(inputPos);
      final BlockEntity outputBlockEntity = outputLevel.getBlockEntity(outputPos);
      if (inputBlockEntity == null || outputBlockEntity == null) return;

      inputBlockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputItemHandler ->
        outputBlockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputItemHandler -> {
          int transferredItems = 0;
          for (int inputSlotIndex = 0; inputSlotIndex < inputItemHandler.getSlots(); ++inputSlotIndex) {
            final ItemStack inputStack = inputItemHandler.getStackInSlot(inputSlotIndex);
            boolean filtered = filterScheme != Mapping.FilterScheme.BLACK_LIST;
            for (int i = 0; i < filterInventory.getContainerSize(); ++i) {
              if (filterInventory.getItem(i).getItem() == inputStack.getItem()) {
                filtered = !filtered;
                break;
              }
            }
            if (filtered) continue;
            for (int outputSlotIndex = 0; outputSlotIndex < outputItemHandler.getSlots(); ++outputSlotIndex) {
              if (transferredItems >= mapping.getItemsPerTick()) return;
              final ItemStack outputStack = outputItemHandler.getStackInSlot(outputSlotIndex);
              if (outputItemHandler.isItemValid(outputSlotIndex, inputStack) && !inputStack.isEmpty() && outputStack.isEmpty() || (!inputStack.isEmpty() && inputStack.getItem() == outputStack.getItem() && outputStack.getCount() < outputStack.getMaxStackSize())) {
                final ItemStack simulatedExtractedIS = inputItemHandler.extractItem(inputSlotIndex, Mth.clamp(inputStack.getCount(), 0, mapping.getItemsPerTick() - transferredItems), true);
                final ItemStack simulatedInsertedLeftoversIS = outputItemHandler.insertItem(outputSlotIndex, simulatedExtractedIS, true);
                final ItemStack extractedIS = inputItemHandler.extractItem(inputSlotIndex, simulatedExtractedIS.getCount() - simulatedInsertedLeftoversIS.getCount(), false);
                final ItemStack insertedIS = outputItemHandler.insertItem(outputSlotIndex, extractedIS, false);
                transferredItems += extractedIS.getCount() - insertedIS.getCount();
                inputBlockEntity.setChanged();
                outputBlockEntity.setChanged();
              }
            }
          }
        }));

      inputBlockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputFluidHandler ->
        outputBlockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputFluidHandler -> {
          int transferredFluids = 0;
          for (int inputSlotIndex = 0; inputSlotIndex < inputFluidHandler.getTanks(); ++inputSlotIndex) {
            final FluidStack inputStack = inputFluidHandler.getFluidInTank(inputSlotIndex);
            boolean filtered = filterScheme != Mapping.FilterScheme.BLACK_LIST;
            for (int i = 0; i < filterInventory.getContainerSize(); ++i) {
              if (inputStack.isFluidEqual(filterInventory.getItem(i))) {
                filtered = !filtered;
                break;
              }
            }
            if (filtered) continue;
            for (int outputSlotIndex = 0; outputSlotIndex < outputFluidHandler.getTanks(); ++outputSlotIndex) {
              if (transferredFluids >= mapping.getBucketsPerTick()) return;
              final FluidStack outputStack = outputFluidHandler.getFluidInTank(outputSlotIndex);
              if (outputFluidHandler.isFluidValid(outputSlotIndex, inputStack)) {
                final FluidStack simulatedDrainedFS = inputFluidHandler.drain(Mth.clamp(inputStack.getAmount(), 0, mapping.getBucketsPerTick() - transferredFluids), IFluidHandler.FluidAction.SIMULATE);
                final int simulatedFilled = outputFluidHandler.fill(simulatedDrainedFS, IFluidHandler.FluidAction.SIMULATE);
                final FluidStack drainedFS = inputFluidHandler.drain(simulatedFilled, IFluidHandler.FluidAction.EXECUTE);
                transferredFluids += outputFluidHandler.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);
                inputBlockEntity.setChanged();
                outputBlockEntity.setChanged();
                break;
              }
            }
          }
        }));

      inputBlockEntity.getCapability(CapabilityEnergy.ENERGY, inputNodeInfo.getFace()).ifPresent(inputEnergyHandler ->
        outputBlockEntity.getCapability(CapabilityEnergy.ENERGY, outputNodeInfo.getFace()).ifPresent(outputEnergyHandler -> {
          int transferredEnergy = 0;
          if (inputEnergyHandler.canExtract() && outputEnergyHandler.canReceive()) {
            final int simulatedExtracted = inputEnergyHandler.extractEnergy(Mth.clamp(inputEnergyHandler.getEnergyStored(), 0, mapping.getEnergyPerTick() - transferredEnergy), true);
            final int simulatedReceived = outputEnergyHandler.receiveEnergy(simulatedExtracted, true);
            final int extracted = inputEnergyHandler.extractEnergy(simulatedReceived, false);
            transferredEnergy += outputEnergyHandler.receiveEnergy(extracted, false);
          }
        }));

      if (mapping.getDistributionScheme() == Mapping.DistributionScheme.CYCLIC) {
        mapping.currentInputIndex = (mapping.currentInputIndex + 1) % Math.max(mapping.getInputs().size(), 1);
        mapping.currentOutputIndex = (mapping.currentOutputIndex + 1) % Math.max(mapping.getOutputs().size(), 1);
      } else if (mapping.getDistributionScheme() == Mapping.DistributionScheme.NATURAL) {
        ++mapping.currentOutputIndex;
        if (mapping.currentOutputIndex >= mapping.getOutputs().size()) {
          mapping.currentOutputIndex = 0;
          ++mapping.currentInputIndex;
          if (mapping.currentInputIndex >= mapping.getInputs().size())
            mapping.currentInputIndex = 0;
        }
      } else if (mapping.getDistributionScheme() == Mapping.DistributionScheme.RANDOM) {
        mapping.currentInputIndex = random.nextInt(mapping.getInputs().size());
        mapping.currentOutputIndex = random.nextInt(mapping.getOutputs().size());
      }
    }
  }

  @Override
  @ParametersAreNonnullByDefault
  public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player player) {
    return player.isCrouching() ? new RouterStorageContainer(windowID, inventory, this) : new RouterContainer(windowID, inventory, this);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  protected AbstractContainerMenu createMenu(int windowID, Inventory inventory) {
    return inventory.player.isCrouching() ?
    new RouterStorageContainer(windowID, inventory, this): new RouterContainer(windowID, inventory, this);
  }

  @Override
  @Nonnull
  public CompoundTag save(CompoundTag compound) {
    compound.putInt("tier", tier.baseTier.ordinal());
    compound.put("energyStorage", energyStorage.serializeNBT());
    writeMappingsNBT(compound);
    writeInventoryNBT(compound);
    return super.save(compound);
  }

  /**
   * Writes the router's inventory to a nbt tag.
   *
   * @param compound The nbt tag to write to.
   */
  public void writeInventoryNBT(CompoundTag compound) {
    if (!this.trySaveLootTable(compound))
      ContainerHelper.saveAllItems(compound, inventory);
  }

  /**
   * Writes the router's mappings to a nbt tag.
   *
   * @param compound The nbt tag to write to.
   */
  private void writeMappingsNBT(CompoundTag compound) {
    Mapping.write(mappings, compound);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void load(CompoundTag nbtIn) {
    setTier(BaseTier.valueOf(nbtIn.getInt("tier")));
    Tag energyStorageNBT = nbtIn.get("energyStorage");
    if (energyStorageNBT != null) {
      energyStorage.deserializeNBT(energyStorageNBT);
    }
    readInventory(nbtIn);
    mappings = Mapping.read(nbtIn, identifiers, tier);
    super.load(nbtIn);
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
    for (ItemStack is : inventory)
      cacheIfIdentifier(is);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    energyCapabilityLO.invalidate();
  }

  @Override
  @Nonnull
  public CompoundTag getUpdateTag() {
    CompoundTag nbt = super.getUpdateTag();
    nbt.putInt("tier", tier.baseTier.ordinal());
    return nbt;
  }

  @Override
  public void handleUpdateTag(CompoundTag nbt) {
    setTier(BaseTier.valueOf(nbt.getInt("tier")));
    super.handleUpdateTag(nbt);
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
    for (ItemStack is : inventory)
      cacheIfIdentifier(is);
    cacheIfIdentifier(itemStack);
    super.setItem(index, itemStack);
  }

  /**
   * If the item has identifier capabilities, cache the capability information.
   *
   * @param is The item stack to check.
   */
  private void cacheIfIdentifier(ItemStack is) {
    if (is.getItem() == Items.IDENTIFIER)
      is.getCapability(Capabilities.IDENTIFIER_CAPABILITY, null).ifPresent(identifierCapability ->
        identifiers.put(is.getHoverName().getContents(), identifierCapability));
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
    return CapabilityEnergy.ENERGY.orEmpty(cap, energyCapabilityLO);
  }

  /**
   * Reads mappings in from nbt data. If the client has the gui open, then update the gui.
   *
   * @param nbt The nbt data to read from.
   */
  public void readMappings(CompoundTag nbt) {
    mappings = Mapping.read(nbt, identifiers, tier);
    if (level.isClientSide()) {
      Screen currentTmp = Minecraft.getInstance().screen;
      if (currentTmp instanceof RouterScreen current) {
        current.update();
      }
    }
  }

  public ArrayList<Mapping> getMappings() {
    return mappings;
  }

  @Override
  @Nonnull
  protected NonNullList<ItemStack> getItems() {
    return inventory;
  }

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

  public RouterTier getTier() {
    return tier;
  }

  /**
   * Sets the router's tier and updates the energy storage capability and tieredRouter block.
   * @param baseTier The tier to be set to.
   */
  private void setTier(@Nonnull BaseTier baseTier) {
    this.tier = new RouterTier(baseTier);
    tieredRouter = switch (this.tier.baseTier) {
      case BASIC:
        yield Blocks.BASIC_ROUTER;
      case ADVANCED:
        yield Blocks.ADVANCED_ROUTER;
      case ELITE:
        yield Blocks.ELITE_ROUTER;
      case ULTIMATE:
        yield  Blocks.ULTIMATE_ROUTER;
      default:
        yield null;
    };
    containerSize = switch (this.tier.baseTier) {
      case BASIC:
        yield 3;
      case ADVANCED:
          yield 6;
      case ELITE:
        yield 9;
      case ULTIMATE:
        yield 12;
      default:
        yield 0;
    };
    inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    energyStorage.setCapacity(this.tier.maxEnergy);
    energyStorage.setMaxReceive(this.tier.maxEnergyPerTick);
  }

  public DynamicEnergyStorage getEnergyStorage() {
    return energyStorage;
  }

  @Override
  public int getContainerSize() {
    return containerSize;
  }

}
