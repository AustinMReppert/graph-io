package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;
import xyz.austinmreppert.graph_io.block.Blocks;
import xyz.austinmreppert.graph_io.container.ControllerNodeContainer;

import java.util.ArrayList;

public class ControllerNodeTE extends TileEntity implements ITickableTileEntity, INamedContainerProvider {

  private ArrayList<String> mappings;

  public ControllerNodeTE() {
    super(TileEntityTypes.CONTROLLER_NODE);
    mappings = new ArrayList<>();
  }

  @Override
  public void tick() {
  }

  @Override
  public ITextComponent getDisplayName() {
    return new TranslationTextComponent(Blocks.CONTROLLER_NODE_BLOCK.getTranslationKey());
  }

  @Override
  public Container createMenu(int windowID, PlayerInventory inventory, PlayerEntity p_createMenu_3_) {
    ControllerNodeContainer controllerNodeContainer = new ControllerNodeContainer(windowID, inventory, this);
    return controllerNodeContainer;
  }

  @Override
  public CompoundNBT write(CompoundNBT compound) {
    System.out.println("Saving data");
    getNBTFromMappings(compound);
    return super.write(compound);
  }

  private CompoundNBT getNBTFromMappings(CompoundNBT compound) {
    ListNBT list = new ListNBT();
    for (String s : mappings) {
      CompoundNBT mapping = new CompoundNBT();
      mapping.putString("mapping", s);
      list.add(mapping);
    }
    compound.put("mappings", list);
    return compound;
  }

  @Override
  public void read(BlockState stateIn, CompoundNBT nbtIn) {
    getMappingsFromNBT(nbtIn);
    super.read(stateIn, nbtIn);
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
      mappings.add(mapping.getString("mapping"));
    }
  }

  @Override
  public SUpdateTileEntityPacket getUpdatePacket(){
    CompoundNBT nbtTag = new CompoundNBT();
    return new SUpdateTileEntityPacket(getPos(), -1, getNBTFromMappings(nbtTag));
  }

  @Override
  public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
    getMappingsFromNBT(pkt.getNbtCompound());
  }

  public void setMappings(ArrayList<String> mappings) {
    this.mappings = mappings;
  }

  public ArrayList<String> getMappings() {
    return mappings;
  }
}
