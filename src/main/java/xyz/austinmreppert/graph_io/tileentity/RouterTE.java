package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
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

public class RouterTE extends LockableLootTileEntity implements ITickableTileEntity, INamedContainerProvider {

  private final Random random;
  private final HashMap<String, BlockPos> identifiers;
  private final DynamicEnergyStorage energyStorage;
  private final LazyOptional<IEnergyStorage> energyCapabilityLO;
  private ArrayList<Mapping> mappings;
  private NonNullList<ItemStack> inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
  private int ticks;
  private RouterTier tier;
  protected Block tieredRouter;
  private int lastTick;
  private int energyPerMappingTick = 1000;

  public RouterTE() {
    super(TileEntityTypes.ROUTER);
    identifiers = new HashMap<>();
    random = new Random(System.currentTimeMillis());
    mappings = new ArrayList<>();
    ticks = 0;
    energyStorage = new DynamicEnergyStorage(0);
    energyCapabilityLO = LazyOptional.of(() -> energyStorage);
  }

  public RouterTE(BaseTier tier) {
    this();
    setTier(tier);
  }

  public boolean shouldTick() {
    if (ticks - lastTick >= tier.minTickDelay) {
      lastTick = ticks;
      return true;
    }
    return false;
  }

  @Override
  public void tick() {
    if (world == null || world.isRemote) return;
    ++ticks;

    if (shouldTick()) {
      for (Direction direction : Direction.values()) {
        if (energyStorage.getEnergyStored() == energyStorage.getMaxEnergyStored())
          break;
        TileEntity te = world.getTileEntity(pos.offset(direction));
        if (te != null) {
          te.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).ifPresent(cap -> {
            int extracted = -1;
            final int simulatedReceived = energyStorage.receiveEnergy(extracted = cap.extractEnergy(tier.maxEnergyPerTick, true), true);
            energyStorage.receiveEnergy(cap.extractEnergy(simulatedReceived, false), false);
            te.markDirty();
            markDirty();
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

      Inventory filterInventory = mapping.getFilterInventory();
      Mapping.FilterScheme filterScheme = mapping.getFilterScheme();
      if (mapping.getInputs().isEmpty() || mapping.getOutputs().isEmpty()) continue;

      final NodeInfo inputNodeInfo = mapping.getInputs().get(mapping.currentInputIndex);
      final NodeInfo outputNodeInfo = mapping.getOutputs().get(mapping.currentOutputIndex);
      final BlockPos inputPos = identifiers.get(inputNodeInfo.getIdentifier());
      final BlockPos outputPos = identifiers.get(outputNodeInfo.getIdentifier());
      if (inputPos == null || outputPos == null || (inputPos.equals(outputPos) && inputNodeInfo.getFace() == outputNodeInfo.getFace()))
        continue;

      final TileEntity inputTE = world.getTileEntity(inputPos);
      final TileEntity outputTE = world.getTileEntity(outputPos);
      if (inputTE == null || outputTE == null) return;

      inputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputItemHandler ->
        outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputItemHandler -> {
          int transferredItems = 0;
          for (int inputSlotIndex = 0; inputSlotIndex < inputItemHandler.getSlots(); ++inputSlotIndex) {
            final ItemStack inputStack = inputItemHandler.getStackInSlot(inputSlotIndex);
            boolean filtered = filterScheme != Mapping.FilterScheme.BLACK_LIST;
            for (int i = 0; i < filterInventory.getSizeInventory(); ++i) {
              if (filterInventory.getStackInSlot(i).getItem() == inputStack.getItem()) {
                filtered = !filtered;
                break;
              }
            }
            if (filtered) continue;
            for (int outputSlotIndex = 0; outputSlotIndex < outputItemHandler.getSlots(); ++outputSlotIndex) {
              if (transferredItems >= mapping.getItemsPerTick()) return;
              final ItemStack outputStack = outputItemHandler.getStackInSlot(outputSlotIndex);
              if (outputItemHandler.isItemValid(outputSlotIndex, inputStack) && !inputStack.isEmpty() && outputStack.isEmpty() || (!inputStack.isEmpty() && inputStack.getItem() == outputStack.getItem() && outputStack.getCount() < outputStack.getMaxStackSize())) {
                final ItemStack simulatedExtractedIS = inputItemHandler.extractItem(inputSlotIndex, MathHelper.clamp(inputStack.getCount(), 0, mapping.getItemsPerTick() - transferredItems), true);
                final ItemStack simulatedInsertedLeftoversIS = outputItemHandler.insertItem(outputSlotIndex, simulatedExtractedIS, true);
                final ItemStack extractedIS = inputItemHandler.extractItem(inputSlotIndex, simulatedExtractedIS.getCount() - simulatedInsertedLeftoversIS.getCount(), false);
                final ItemStack insertedIS = outputItemHandler.insertItem(outputSlotIndex, extractedIS, false);
                transferredItems += extractedIS.getCount() - insertedIS.getCount();
                inputTE.markDirty();
                outputTE.markDirty();
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
            for (int i = 0; i < filterInventory.getSizeInventory(); ++i) {
              if (inputStack.isFluidEqual(filterInventory.getStackInSlot(i))) {
                filtered = !filtered;
                break;
              }
            }
            if (filtered) continue;
            for (int outputSlotIndex = 0; outputSlotIndex < outputFluidHandler.getTanks(); ++outputSlotIndex) {
              if (transferredFluids >= mapping.getBucketsPerTick()) return;
              final FluidStack outputStack = outputFluidHandler.getFluidInTank(outputSlotIndex);
              if (outputFluidHandler.isFluidValid(outputSlotIndex, inputStack)) {
                final FluidStack simulatedDrainedFS = inputFluidHandler.drain(MathHelper.clamp(inputStack.getAmount(), 0, mapping.getBucketsPerTick() - transferredFluids), IFluidHandler.FluidAction.SIMULATE);
                final int simulatedFilled = outputFluidHandler.fill(simulatedDrainedFS, IFluidHandler.FluidAction.SIMULATE);
                final FluidStack drainedFS = inputFluidHandler.drain(simulatedFilled, IFluidHandler.FluidAction.EXECUTE);
                transferredFluids += outputFluidHandler.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);
                inputTE.markDirty();
                outputTE.markDirty();
                break;
              }
            }
          }
        }));

      inputTE.getCapability(CapabilityEnergy.ENERGY, inputNodeInfo.getFace()).ifPresent(inputEnergyHandler ->
        outputTE.getCapability(CapabilityEnergy.ENERGY, outputNodeInfo.getFace()).ifPresent(outputEnergyHandler -> {
          int transferredEnergy = 0;
          if (inputEnergyHandler.canExtract() && outputEnergyHandler.canReceive()) {
            final int simulatedExtracted = inputEnergyHandler.extractEnergy(MathHelper.clamp(inputEnergyHandler.getEnergyStored(), 0, mapping.getEnergyPerTick() - transferredEnergy), true);
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
  public Container createMenu(int windowID, PlayerInventory inventory, PlayerEntity p_createMenu_3_) {
    return p_createMenu_3_.isSneaking() ? ChestContainer.createGeneric9X6(windowID, inventory, this) : new RouterContainer(windowID, inventory, this);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  protected Container createMenu(int windowID, PlayerInventory inventory) {
    return inventory.player.isSneaking() ? ChestContainer.createGeneric9X6(windowID, inventory, this) : new RouterContainer(windowID, inventory, this);
  }

  @Override
  @Nonnull
  public CompoundNBT write(CompoundNBT compound) {
    compound.putInt("tier", tier.baseTier.ordinal());
    compound.put("energyStorage", CapabilityEnergy.ENERGY.writeNBT(energyStorage, null));
    writeMappingsNBT(compound);
    writeInventoryNBT(compound);
    return super.write(compound);
  }

  /**
   * Writes the router's inventory to an nbt tag.
   *
   * @param compound The nbt tag to write to.
   */
  public void writeInventoryNBT(CompoundNBT compound) {
    if (!this.checkLootAndWrite(compound))
      ItemStackHelper.saveAllItems(compound, inventory);
  }

  /**
   * Writes the router's mappings to an nbt tag.
   *
   * @param compound The nbt tag to write to.
   */
  private void writeMappingsNBT(CompoundNBT compound) {
    Mapping.write(mappings, compound);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void read(BlockState stateIn, CompoundNBT nbtIn) {
    setTier(BaseTier.valueOf(nbtIn.getInt("tier")));
    INBT energyStorageNBT = nbtIn.get("energyStorage");
    if (energyStorageNBT != null) {
      CapabilityEnergy.ENERGY.readNBT(energyStorage, null, energyStorageNBT);
    }
    readInventory(nbtIn);
    mappings = Mapping.read(nbtIn, identifiers, tier);
    super.read(stateIn, nbtIn);
  }

  /**
   * Reads the inventory in from nbt data.
   *
   * @param nbt The nbt data to read from.
   */
  public void readInventory(CompoundNBT nbt) {
    setItems(NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY));
    if (!this.checkLootAndRead(nbt))
      ItemStackHelper.loadAllItems(nbt, inventory);
    identifiers.clear();
    for (ItemStack is : inventory)
      cacheIfIdentifier(is);
  }

  @Override
  public void remove() {
    super.remove();
    energyCapabilityLO.invalidate();
  }

  @Override
  @Nonnull
  public CompoundNBT getUpdateTag() {
    CompoundNBT nbt = super.getUpdateTag();
    nbt.putInt("tier", tier.baseTier.ordinal());
    return nbt;
  }

  @Override
  public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
    setTier(BaseTier.valueOf(nbt.getInt("tier")));
    super.handleUpdateTag(state, nbt);
  }

  @Override
  @Nonnull
  public ItemStack removeStackFromSlot(int index) {
    identifiers.clear();
    for (ItemStack is : inventory)
      cacheIfIdentifier(is);
    return super.removeStackFromSlot(index);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void setInventorySlotContents(int index, ItemStack itemStack) {
    identifiers.clear();
    for (ItemStack is : inventory)
      cacheIfIdentifier(is);
    cacheIfIdentifier(itemStack);
    super.setInventorySlotContents(index, itemStack);
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
  public void readMappings(CompoundNBT nbt) {
    mappings = Mapping.read(nbt, identifiers, tier);
    if (world.isRemote()) {
      Screen currentTmp = Minecraft.getInstance().currentScreen;
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
  public ITextComponent getDisplayName() {
    return tieredRouter.getTranslatedName();
  }

  @Override
  @Nonnull
  protected ITextComponent getDefaultName() {
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
    return energyStorage;
  }

  @Override
  public int getSizeInventory() {
    return 56;
  }

}
