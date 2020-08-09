package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import xyz.austinmreppert.graph_io.block.Blocks;
import xyz.austinmreppert.graph_io.capabilities.Capabilities;
import xyz.austinmreppert.graph_io.container.ControllerNodeContainer;
import xyz.austinmreppert.graph_io.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ControllerNodeTE extends LockableLootTileEntity implements ITickableTileEntity, INamedContainerProvider {

  private ArrayList<Mapping> mappings;
  private HashMap<String, BlockPos> identifiers;
  private NonNullList<ItemStack> inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
  private int ticks;
  private int maxItemsPerTick = 64;
  private int maxBucketsPerTick = 1000;
  private int maxEnergyPerTick = 4000;
  private int filterSize = 10;
  private final Random random;
  private Tier tier;

  public int getMaxBucketsPerTick() {
    return maxBucketsPerTick;
  }

  public int getMaxEnergyPerTick() {
    return maxEnergyPerTick;
  }

  public enum Tier {
    WOOD,
    IRON,
    GOLD,
    DIAMOND;

    public static ControllerNodeTE.Tier valueOf(int ordinal) {
      if (ordinal == 0) return Tier.WOOD;
      else if (ordinal == 1) return Tier.IRON;
      else if (ordinal == 2) return Tier.GOLD;
      else if (ordinal == 3) return Tier.DIAMOND;
      else return null;
    }

  }

  public ControllerNodeTE() {
    this(Tier.WOOD);
  }

  public ControllerNodeTE(Tier tier) {
    super(TileEntityTypes.CONTROLLER_NODE);
    mappings = new ArrayList<>();
    identifiers = new HashMap<>();
    ticks = 0;
    random = new Random(System.currentTimeMillis());
    setTier(tier);

  }

  private void setTier(Tier tier) {
    this.tier = tier;
    if (tier == Tier.WOOD) {
      maxItemsPerTick = 1;
      maxBucketsPerTick = 1000;
      maxEnergyPerTick = 400;
      filterSize = 5;
    } else if (tier == Tier.IRON) {
      maxItemsPerTick = 16;
      maxBucketsPerTick = 4000;
      maxEnergyPerTick = 4000;
      filterSize = 10;
    } else if (tier == Tier.GOLD) {
      maxItemsPerTick = 32;
      maxBucketsPerTick = 8000;
      maxEnergyPerTick = 40000;
    } else if (tier == Tier.DIAMOND) {
      maxItemsPerTick = 64;
      maxBucketsPerTick = 16000;
      maxEnergyPerTick = 400000;
    }
  }

  @Override
  public void tick() {
    if (world == null || world.isRemote || ++ticks < 20*5) return;
    ticks = 0;
    for (Mapping mapping : mappings) {
      if (!mapping.isValid())
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
  @Nonnull
  public ITextComponent getDisplayName() {
    if (tier == Tier.WOOD) {
      return new TranslationTextComponent(Blocks.WOOD_CONTROLLER_NODE_BLOCK.getTranslationKey());
    } else if (tier == Tier.IRON) {
      return new TranslationTextComponent(Blocks.WOOD_CONTROLLER_NODE_BLOCK.getTranslationKey());
    } else if (tier == Tier.GOLD) {
      return new TranslationTextComponent(Blocks.WOOD_CONTROLLER_NODE_BLOCK.getTranslationKey());
    } else if (tier == Tier.DIAMOND) {
      return new TranslationTextComponent(Blocks.WOOD_CONTROLLER_NODE_BLOCK.getTranslationKey());
    }
    return new TranslationTextComponent(Blocks.WOOD_CONTROLLER_NODE_BLOCK.getTranslationKey());
  }

  @Override
  @Nonnull
  protected ITextComponent getDefaultName() {
    return Blocks.WOOD_CONTROLLER_NODE_BLOCK.getTranslatedName();
  }

  @Override
  @ParametersAreNonnullByDefault
  public Container createMenu(int windowID, PlayerInventory inventory, PlayerEntity p_createMenu_3_) {
    return p_createMenu_3_.isSneaking() ? ChestContainer.createGeneric9X6(windowID, inventory, this) : new ControllerNodeContainer(windowID, inventory, this);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  protected Container createMenu(int windowID, PlayerInventory inventory) {
    return inventory.player.isSneaking() ? ChestContainer.createGeneric9X6(windowID, inventory, this) : new ControllerNodeContainer(windowID, inventory, this);
  }

  @Override
  @Nonnull
  public CompoundNBT write(CompoundNBT compound) {
    compound.putInt("tier", getTierOrdinal());
    getNBTFromMappings(compound);
    getNBTFromInventory(compound);
    return super.write(compound);
  }

  public void getNBTFromInventory(CompoundNBT compound) {
    super.write(compound);
    if (!this.checkLootAndWrite(compound))
      ItemStackHelper.saveAllItems(compound, inventory);
  }

  private void getNBTFromMappings(CompoundNBT compound) {
    Mapping.toNBT(mappings, compound);
  }

  public int getTierOrdinal() {
    if(tier == null) return -1;
    return tier.ordinal();
  }

  @Override
  @ParametersAreNonnullByDefault
  public void read(BlockState stateIn, CompoundNBT nbtIn) {
    setTier(Tier.valueOf(nbtIn.getInt("tier")));
    getInventoryFromNBT(nbtIn);
    mappings = Mapping.getMappingsFromNBT(nbtIn, identifiers, filterSize, maxItemsPerTick, maxBucketsPerTick, maxEnergyPerTick);
    super.read(stateIn, nbtIn);
  }

  public void getInventoryFromNBT(CompoundNBT nbt) {
    setItems(NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY));
    if (!this.checkLootAndRead(nbt))
      ItemStackHelper.loadAllItems(nbt, inventory);
    identifiers.clear();
    for (ItemStack is : inventory)
      checkForIdentifier(is);
  }

  @Override
  @Nonnull
  public CompoundNBT getUpdateTag() {
    CompoundNBT mappingsNBT = super.getUpdateTag();
    mappingsNBT.putInt("tier", getTierOrdinal());
    getNBTFromMappings(mappingsNBT);
    return mappingsNBT;
  }

  @Override
  public void handleUpdateTag(BlockState state, CompoundNBT tag) {
    mappings = Mapping.getMappingsFromNBT(tag, identifiers, filterSize, maxItemsPerTick, maxBucketsPerTick, maxEnergyPerTick);
    super.handleUpdateTag(state, tag);
  }

  @Override
  public SUpdateTileEntityPacket getUpdatePacket() {
    CompoundNBT nbtTag = new CompoundNBT();
    nbtTag.putInt("tier", getTierOrdinal());
    getNBTFromMappings(nbtTag);
    return new SUpdateTileEntityPacket(getPos(), -1, nbtTag);
  }

  @Override
  public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
    mappings = Mapping.getMappingsFromNBT(pkt.getNbtCompound(), identifiers, filterSize, maxItemsPerTick, maxBucketsPerTick, maxEnergyPerTick);
  }

  public ArrayList<Mapping> getMappings() {
    return mappings;
  }

  @Override
  public int getSizeInventory() {
    return 56;
  }

  @Override
  @Nonnull
  protected NonNullList<ItemStack> getItems() {
    return inventory;
  }

  @Override
  @Nonnull
  public ItemStack removeStackFromSlot(int index) {
    identifiers.clear();
    for (ItemStack is : inventory)
      checkForIdentifier(is);
    return super.removeStackFromSlot(index);
  }

  @Override
  @Nonnull
  public ItemStack decrStackSize(int index, int count) {
    return super.decrStackSize(index, count);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void setInventorySlotContents(int index, ItemStack itemStack) {
    identifiers.clear();
    for (ItemStack is : inventory)
      checkForIdentifier(is);
    checkForIdentifier(itemStack);
    super.setInventorySlotContents(index, itemStack);
  }

  private void checkForIdentifier(ItemStack is) {
    if (is.getItem() == Items.IDENTIFIER)
      is.getCapability(Capabilities.IDENTIFIER_CAPABILITY, null).ifPresent(identifierCapability ->
        identifiers.put(is.getDisplayName().getString(), identifierCapability.getBlockPos()));
  }

  @Override
  protected void setItems(NonNullList<ItemStack> itemsIn) {
    identifiers.clear();
    for (ItemStack is : itemsIn)
      checkForIdentifier(is);
    this.inventory = itemsIn;
  }

  public int getFilterSize() {
    return filterSize;
  }

  public void getMappingsFromNBT(CompoundNBT controllerNodeTENBT) {
    mappings = Mapping.getMappingsFromNBT(controllerNodeTENBT, identifiers, filterSize, maxItemsPerTick, maxBucketsPerTick, maxEnergyPerTick);
  }

  public int getMaxItemsPerTick() {
    return maxItemsPerTick;
  }
}
