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

public class ControllerNodeTE extends LockableLootTileEntity implements ITickableTileEntity, INamedContainerProvider {

  private ArrayList<Mapping> mappings;
  private HashMap<String, BlockPos> identifiers;
  private NonNullList<ItemStack> inventory = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
  private int ticks;

  public ControllerNodeTE() {
    super(TileEntityTypes.CONTROLLER_NODE);
    mappings = new ArrayList<>();
    identifiers = new HashMap<>();
    ticks = 0;
  }

  // chest -> *
  // chest -> furnace
  // chest.back -> furnace.side
  // chest -> *.*
  @Override
  public void tick() {
    if (world == null || world.isRemote) return;
    ++ticks;
    if (ticks < 20) return;
    ticks = 0;
    for (Mapping mapping : mappings) {
      for (NodeInfo input : mapping.getInputs()) {
        for (NodeInfo output : mapping.getOutputs()) {
          BlockPos inputPos = identifiers.get(input.getIdentifier());
          BlockPos outputPos = identifiers.get(output.getIdentifier());
          if (inputPos == null || outputPos == null || inputPos.equals(outputPos)) continue;
          TileEntity inputTE = world.getTileEntity(inputPos);
          TileEntity outputTE = world.getTileEntity(outputPos);
          if (inputTE == null || outputTE == null) return;
          inputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, input.getFace()).ifPresent(chestCapability -> {
            outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, output.getFace()).ifPresent(furnaceCapability -> {
              for (int i = 0; i < chestCapability.getSlots(); ++i) {
                ItemStack is = chestCapability.getStackInSlot(i);
                for (int j = 0; j < furnaceCapability.getSlots(); ++j) {
                  ItemStack is2 = furnaceCapability.getStackInSlot(j);
                  if (furnaceCapability.isItemValid(j, is) && !is.isEmpty() && is2.isEmpty() || (is.getItem() == is2.getItem() && is2.getCount() < is2.getMaxStackSize())) {
                    furnaceCapability.insertItem(j, chestCapability.extractItem(i, MathHelper.clamp(is.getCount(), 0, is2.getMaxStackSize() - is2.getCount()), false), false);
                    inputTE.markDirty();
                    outputTE.markDirty();
                    break;
                  }
                }
              }
            });
          });
          inputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, input.getFace()).ifPresent(inputCapability -> {
            outputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, output.getFace()).ifPresent(outputCapability -> {
              for (int i = 0; i < inputCapability.getTanks(); ++i) {
                FluidStack is = inputCapability.getFluidInTank(i);
                for (int j = 0; j < outputCapability.getTanks(); ++j) {
                  FluidStack is2 = outputCapability.getFluidInTank(j);
                  if (outputCapability.isFluidValid(j, is)) {
                    int original = is.getAmount();
                    int filled = outputCapability.fill(inputCapability.drain(is.getAmount(), IFluidHandler.FluidAction.SIMULATE), IFluidHandler.FluidAction.SIMULATE);
                    outputCapability.fill(inputCapability.drain(filled, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                    inputTE.markDirty();
                    outputTE.markDirty();
                    break;
                  }
                }
              }
            });
          });
        }
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
    ListNBT list = new ListNBT();
    for (Mapping mapping : mappings) {
      CompoundNBT mappingNBT = new CompoundNBT();
      mappingNBT.putString("mapping", mapping.getRaw());
      list.add(mappingNBT);
    }
    compound.put("mappings", list);
    return compound;
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
      mappings.add(new Mapping(mapping.getString("mapping"), identifiers.keySet()));
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
  public void setInventorySlotContents(int index, ItemStack is) {
    checkForIdentifier(is);
    super.setInventorySlotContents(index, is);
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
    for (ItemStack is : itemsIn)
      checkForIdentifier(is);
    this.inventory = itemsIn;
  }
}
