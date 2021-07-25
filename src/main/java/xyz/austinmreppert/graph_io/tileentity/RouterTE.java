package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.logging.log4j.LogManager;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.block.Blocks;
import xyz.austinmreppert.graph_io.capabilities.Capabilities;
import xyz.austinmreppert.graph_io.capabilities.DynamicEnergyStorage;
import xyz.austinmreppert.graph_io.client.gui.RouterScreen;
import xyz.austinmreppert.graph_io.container.RouterContainer;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;
import xyz.austinmreppert.graph_io.data.mappings.NodeInfo;
import xyz.austinmreppert.graph_io.data.tiers.RouterTier;
import xyz.austinmreppert.graph_io.data.tiers.BaseTier;
import xyz.austinmreppert.graph_io.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class RouterTE extends RandomizableContainerBlockEntity implements MenuProvider {

  private Random random;
  private HashMap<String, BlockPos> identifiers;
  private DynamicEnergyStorage energyStorage;
  private LazyOptional<IEnergyStorage> energyCapabilityLO;
  private ArrayList<Mapping> mappings;
  private NonNullList<ItemStack> inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
  private int ticks;
  private RouterTier tier;
  protected Block tieredRouter;
  private int lastTick;
  private int energyPerMappingTick = 1000;

  public RouterTE(BlockPos pos, BlockState state) {
    super(TileEntityTypes.ROUTER, pos, state);
    identifiers = new HashMap<>();
    random = new Random(System.currentTimeMillis());
    mappings = new ArrayList<>();
    ticks = 0;
    energyStorage = new DynamicEnergyStorage(0);
    energyCapabilityLO = LazyOptional.of(() -> energyStorage);
  }

  public RouterTE(BaseTier tier, BlockPos pos, BlockState state) {
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

  public void serverTick(BlockPos p_155254_) {
    if (level == null || level.isClientSide) return;
    ++ticks;

    if (shouldTick()) {
      for (Direction direction : Direction.values()) {
        if (energyStorage.getEnergyStored() == energyStorage.getMaxEnergyStored())
          break;
        BlockEntity te = level.getBlockEntity(p_155254_);
        if (te != null) {
          te.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).ifPresent(cap -> {
            int extracted = -1;
            final int simulatedReceived = energyStorage.receiveEnergy(extracted = cap.extractEnergy(tier.maxEnergyPerTick, true), true);
            energyStorage.receiveEnergy(cap.extractEnergy(simulatedReceived, false), false);
            te.setChanged();
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
      final BlockPos inputPos = identifiers.get(inputNodeInfo.getIdentifier());
      final BlockPos outputPos = identifiers.get(outputNodeInfo.getIdentifier());
      //System.out.println(identifiers.size());
      //System.out.println(identifiers.get(inputNodeInfo.getIdentifier()));
      System.out.println(outputPos);
      if (inputPos == null || outputPos == null || (inputPos.equals(outputPos) && inputNodeInfo.getFace() == outputNodeInfo.getFace()))
        continue;

      final BlockEntity inputTE = level.getBlockEntity(inputPos);
      final BlockEntity outputTE = level.getBlockEntity(outputPos);
      if (inputTE == null || outputTE == null) return;

      inputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputItemHandler ->
        outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputItemHandler -> {
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
                inputTE.setChanged();
                outputTE.setChanged();
              }
            }
          }
        }));

      inputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputFluidHandler ->
        outputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputFluidHandler -> {
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
                inputTE.setChanged();
                outputTE.setChanged();
                break;
              }
            }
          }
        }));

      inputTE.getCapability(CapabilityEnergy.ENERGY, inputNodeInfo.getFace()).ifPresent(inputEnergyHandler ->
        outputTE.getCapability(CapabilityEnergy.ENERGY, outputNodeInfo.getFace()).ifPresent(outputEnergyHandler -> {
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
  public AbstractContainerMenu createMenu(int windowID, Inventory inventory, Player p_createMenu_3_) {
    return p_createMenu_3_.isCrouching() ? ChestMenu.sixRows(windowID, inventory, this) : new RouterContainer(windowID, inventory, this);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  protected AbstractContainerMenu createMenu(int windowID, Inventory inventory) {
    return inventory.player.isCrouching() ? ChestMenu.sixRows(windowID, inventory, this) : new RouterContainer(windowID, inventory, this);
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
   * Writes the router's inventory to an nbt tag.
   *
   * @param compound The nbt tag to write to.
   */
  public void writeInventoryNBT(CompoundTag compound) {
    if (!this.trySaveLootTable(compound))
      ContainerHelper.saveAllItems(compound, inventory);
  }

  /**
   * Writes the router's mappings to an nbt tag.
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
        identifiers.put(is.getDisplayName().getString(), identifierCapability.getBlockPos()));
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
      if (currentTmp instanceof RouterScreen) {
        RouterScreen current = (RouterScreen) currentTmp;
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
    switch (this.tier.baseTier) {
      case BASIC:
        tieredRouter = Blocks.BASIC_ROUTER;
      case ADVANCED:
        tieredRouter = Blocks.ADVANCED_ROUTER;
      case ELITE:
        tieredRouter = Blocks.ELITE_ROUTER;
      case ULTIMATE:
        tieredRouter = Blocks.ULTIMATE_ROUTER;
    };
    energyStorage.setCapacity(this.tier.maxEnergy);
    energyStorage.setMaxReceive(this.tier.maxEnergyPerTick);
  }

  public DynamicEnergyStorage getEnergyStorage() {
    energyStorage.setEnergyStored(energyStorage.getMaxEnergyStored());
    return energyStorage;
  }

  @Override
  public int getContainerSize() {
    return 56;
  }

}
