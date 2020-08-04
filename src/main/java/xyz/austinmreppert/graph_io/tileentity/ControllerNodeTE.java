package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import xyz.austinmreppert.graph_io.block.Blocks;
import xyz.austinmreppert.graph_io.capabilities.Capabilities;
import xyz.austinmreppert.graph_io.container.ControllerNodeContainer;
import xyz.austinmreppert.graph_io.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ControllerNodeTE extends LockableLootTileEntity implements ITickableTileEntity, INamedContainerProvider {

  private ArrayList<Mapping> mappings;
  private HashMap<String, BlockPos> identifiers;
  private NonNullList<ItemStack> inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
  private int ticks;
  private int maxItemTransfersPerTick = 64;
  private int maxFluidTransfersPerTick = 1000;
  private int maxEnergyTransfersPerTick = 4000;
  private Random random;

  public ControllerNodeTE() {
    super(TileEntityTypes.CONTROLLER_NODE);
    mappings = new ArrayList<>();
    identifiers = new HashMap<>();
    ticks = 0;
    random = new Random(System.currentTimeMillis());
  }

  @Override
  public void tick() {
    if (world == null || world.isRemote || ++ticks < 20) return;
    ticks = 0;
    for (Mapping mapping : mappings) {
      if (mapping.getInputs().isEmpty() || mapping.getOutputs().isEmpty()) continue;

      System.out.println("(" + mapping.currentInputIndex + "," + mapping.currentOutputIndex + ")");
      System.out.println("sizes (" + mapping.getInputs().size() + "," + mapping.getOutputs().size() + ")");

      final NodeInfo inputNodeInfo = mapping.getInputs().get(mapping.currentInputIndex);
      final NodeInfo outputNodeInfo = mapping.getOutputs().get(mapping.currentOutputIndex);
      final BlockPos inputPos = identifiers.get(inputNodeInfo.getIdentifier());
      final BlockPos outputPos = identifiers.get(outputNodeInfo.getIdentifier());
      if (inputPos == null || outputPos == null || (inputPos.equals(outputPos) && inputNodeInfo.getFace() == outputNodeInfo.getFace()))
        continue;

      final TileEntity inputTE = world.getTileEntity(inputPos);
      final TileEntity outputTE = world.getTileEntity(outputPos);
      if (inputTE == null || outputTE == null) return;

      inputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputItemHandler -> {
        outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputItemHandler -> {
          int transferredItems = 0;
          for (int inputSlotIndex = 0; inputSlotIndex < inputItemHandler.getSlots(); ++inputSlotIndex) {
            final ItemStack inputStack = inputItemHandler.getStackInSlot(inputSlotIndex);
            for (int outputSlotIndex = 0; outputSlotIndex < outputItemHandler.getSlots(); ++outputSlotIndex) {
              if (transferredItems >= maxItemTransfersPerTick) return;
              final ItemStack outputStack = outputItemHandler.getStackInSlot(outputSlotIndex);
              if (outputItemHandler.isItemValid(outputSlotIndex, inputStack) && !inputStack.isEmpty() && outputStack.isEmpty() || (!inputStack.isEmpty() && inputStack.getItem() == outputStack.getItem() && outputStack.getCount() < outputStack.getMaxStackSize())) {
                final ItemStack simulatedExtractedIS = inputItemHandler.extractItem(inputSlotIndex, MathHelper.clamp(inputStack.getCount(), 0, maxItemTransfersPerTick - transferredItems), true);
                final ItemStack simulatedInsertedLeftoversIS = outputItemHandler.insertItem(outputSlotIndex, simulatedExtractedIS, true);
                final ItemStack extractedIS = inputItemHandler.extractItem(inputSlotIndex, simulatedExtractedIS.getCount() - simulatedInsertedLeftoversIS.getCount(), false);
                final ItemStack insertedIS = outputItemHandler.insertItem(outputSlotIndex, extractedIS, false);
                transferredItems += extractedIS.getCount() - insertedIS.getCount();
                inputTE.markDirty();
                outputTE.markDirty();
              }
            }
          }
        });
      });

      inputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inputNodeInfo.getFace()).ifPresent(inputFluidHandler -> {
        outputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, outputNodeInfo.getFace()).ifPresent(outputFluidHandler -> {
          int transferredFluids = 0;
          for (int inputSlotIndex = 0; inputSlotIndex < inputFluidHandler.getTanks(); ++inputSlotIndex) {
            final FluidStack inputStack = inputFluidHandler.getFluidInTank(inputSlotIndex);
            for (int outputSlotIndex = 0; outputSlotIndex < outputFluidHandler.getTanks(); ++outputSlotIndex) {
              if (transferredFluids >= maxFluidTransfersPerTick) return;
              final FluidStack outputStack = outputFluidHandler.getFluidInTank(outputSlotIndex);
              if (outputFluidHandler.isFluidValid(outputSlotIndex, inputStack)) {
                final FluidStack simulatedDrainedFS = inputFluidHandler.drain(MathHelper.clamp(inputStack.getAmount(), 0, maxFluidTransfersPerTick - transferredFluids), IFluidHandler.FluidAction.SIMULATE);
                final int simulatedFilled = outputFluidHandler.fill(simulatedDrainedFS, IFluidHandler.FluidAction.SIMULATE);
                final FluidStack drainedFS = inputFluidHandler.drain(simulatedFilled, IFluidHandler.FluidAction.EXECUTE);
                transferredFluids += outputFluidHandler.fill(drainedFS, IFluidHandler.FluidAction.EXECUTE);
                inputTE.markDirty();
                outputTE.markDirty();
                break;
              }
            }
          }
        });
      });

      inputTE.getCapability(CapabilityEnergy.ENERGY, inputNodeInfo.getFace()).ifPresent(inputEnergyHandler -> {
        outputTE.getCapability(CapabilityEnergy.ENERGY, outputNodeInfo.getFace()).ifPresent(outputEnergyHandler -> {
          int transferredEnergy = 0;
          if (inputEnergyHandler.canExtract() && outputEnergyHandler.canReceive()) {
            final int simulatedExtracted = inputEnergyHandler.extractEnergy(MathHelper.clamp(inputEnergyHandler.getEnergyStored(), 0, maxEnergyTransfersPerTick - transferredEnergy), true);
            final int simulatedReceived = outputEnergyHandler.receiveEnergy(simulatedExtracted, true);
            final int extracted = inputEnergyHandler.extractEnergy(simulatedReceived, false);
            transferredEnergy += outputEnergyHandler.receiveEnergy(extracted, false);
          }
        });

      });

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
  public ITextComponent getDisplayName() {
    return new TranslationTextComponent(Blocks.CONTROLLER_NODE_BLOCK.getTranslationKey());
  }

  @Override
  protected ITextComponent getDefaultName() {
    return null;
  }

  @Override
  public Container createMenu(int windowID, PlayerInventory inventory, PlayerEntity p_createMenu_3_) {
    return p_createMenu_3_.isSneaking() ? ChestContainer.createGeneric9X6(windowID, inventory, this) : new ControllerNodeContainer(windowID, inventory, this);
  }

  @Override
  protected Container createMenu(int windowID, PlayerInventory inventory) {
    if (Minecraft.getInstance().player.isSneaking()) {
      return ChestContainer.createGeneric9X6(windowID, inventory, this);
    } else {
      ControllerNodeContainer controllerNodeContainer = new ControllerNodeContainer(windowID, inventory, this);
      return controllerNodeContainer;
    }
  }

  @Override
  public CompoundNBT write(CompoundNBT compound) {
    getNBTFromMappings(compound);
    getNBTFromInventory(compound);
    return super.write(compound);
  }

  public CompoundNBT getNBTFromInventory(CompoundNBT compound) {
    super.write(compound);
    if (!this.checkLootAndWrite(compound)) {
      ItemStackHelper.saveAllItems(compound, inventory);
    }

    return compound;
  }

  private CompoundNBT getNBTFromMappings(CompoundNBT compound) {
    return Mapping.toNBT(mappings, compound);
  }

  @Override
  public void read(BlockState stateIn, CompoundNBT nbtIn) {
    // swaped
    getInventoryFromNBT(nbtIn);
    getMappingsFromNBT(nbtIn);
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
  public CompoundNBT getUpdateTag() {
    CompoundNBT mappingsNBT = super.getUpdateTag();
    getNBTFromMappings(mappingsNBT);
    return mappingsNBT;
  }

  @Override
  public void handleUpdateTag(BlockState state, CompoundNBT tag) {
    getMappingsFromNBT(tag);
    super.handleUpdateTag(state, tag);
  }

  public void getMappingsFromNBT(CompoundNBT tag) {
    ListNBT list = tag.getList("mappings", Constants.NBT.TAG_COMPOUND);
    mappings.clear();
    for (int i = 0; i < list.size(); ++i) {
      CompoundNBT mapping = list.getCompound(i);
      mappings.add(new Mapping(mapping.getString("mapping"), identifiers.keySet(), Mapping.DistributionScheme.valueOf(mapping.getInt("distributionScheme"))));
    }
  }

  @Override
  public SUpdateTileEntityPacket getUpdatePacket() {
    CompoundNBT nbtTag = new CompoundNBT();
    return new SUpdateTileEntityPacket(getPos(), -1, getNBTFromMappings(nbtTag));
  }

  @Override
  public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
    getMappingsFromNBT(pkt.getNbtCompound());
  }

  public void setMappings(ArrayList<Mapping> mappings) {
    this.mappings = mappings;
  }

  public ArrayList<Mapping> getMappings() {
    return mappings;
  }

  @Override
  public int getSizeInventory() {
    return 56;
  }

  @Override
  protected NonNullList<ItemStack> getItems() {
    return inventory;
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    System.out.println("REMOVED");
    identifiers.clear();
    for (ItemStack is : inventory)
      checkForIdentifier(is);
    return super.removeStackFromSlot(index);
  }

  @Override
  public ItemStack decrStackSize(int index, int count) {
    System.out.println("DECREADSR");
    return super.decrStackSize(index, count);
  }

  @Override
  public void setInventorySlotContents(int index, ItemStack itemStack) {
    identifiers.clear();
    for (ItemStack is : inventory)
      checkForIdentifier(is);
    checkForIdentifier(itemStack);
    super.setInventorySlotContents(index, itemStack);
  }

  private void checkForIdentifier(ItemStack is) {
    if (is.getItem() == Items.IDENTIFIER) {
      is.getCapability(Capabilities.IDENTIFIER_CAPABILITY, null).ifPresent(identifierCapability -> {
        identifiers.put(is.getDisplayName().getString(), identifierCapability.getBlockPos());
      });
    }
  }

  @Override
  protected void setItems(NonNullList<ItemStack> itemsIn) {
    identifiers.clear();
    for (ItemStack is : itemsIn)
      checkForIdentifier(is);
    this.inventory = itemsIn;
  }

}
