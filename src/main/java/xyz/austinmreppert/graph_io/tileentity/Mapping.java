package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Mapping {

  private String raw;
  private ArrayList<NodeInfo> inputs;
  private ArrayList<NodeInfo> outputs;
  private boolean valid;
  private DistributionScheme distributionScheme;
  private FilterScheme filterScheme;
  private Inventory filterInventory;
  public int currentInputIndex;
  public int currentOutputIndex;

  public Mapping(String raw, Set<String> identifiers, DistributionScheme distributionScheme, FilterScheme filterScheme, int filterSize) {
    this.raw = raw;
    this.distributionScheme = distributionScheme;
    this.filterScheme = filterScheme;
    filterInventory = new Inventory(filterSize);
    String[] components = raw.split("->");
    inputs = new ArrayList<>();
    outputs = new ArrayList<>();
    if (components.length != 2) {
      return;
    }
    String[] inputs = components[0].split(",");
    String[] outputs = components[1].split(",");
    for (String input : inputs) {
      ArrayList<NodeInfo> tmp = NodeInfo.getNodeInfo(input, identifiers);
      for (NodeInfo inputNodeInfo : tmp)
        if (!inputNodeInfo.isValid())
          return;
      this.inputs.addAll(tmp);
    }
    for (String output : outputs) {
      ArrayList<NodeInfo> tmp = NodeInfo.getNodeInfo(output, identifiers);
      for (NodeInfo outputNodeInfo : tmp)
        if (!outputNodeInfo.isValid())
          return;
      this.outputs.addAll(tmp);
    }
    valid = true;
  }

  public Mapping(String raw, DistributionScheme distributionScheme, FilterScheme filterScheme, int filterSize) {
    this.raw = raw;
    this.distributionScheme = distributionScheme;
    this.filterScheme = filterScheme;
    filterInventory = new Inventory(filterSize);
  }

  public Mapping(Mapping mapping) {
    this(mapping.getRaw(), mapping.distributionScheme, mapping.filterScheme, mapping.getFilterInventory().getSizeInventory());
    for (int i = 0; i < mapping.filterInventory.getSizeInventory(); ++i)
      filterInventory.setInventorySlotContents(i, mapping.getFilterInventory().getStackInSlot(i).copy());
  }

  public static CompoundNBT toNBT(ArrayList<Mapping> mappingsCopy) {
    CompoundNBT mappingsNBT = new CompoundNBT();
    return Mapping.toNBT(mappingsCopy, mappingsNBT);
  }

  public static CompoundNBT toNBT(ArrayList<Mapping> mappingsCopy, CompoundNBT nbt) {
    ListNBT list = new ListNBT();
    for (Mapping mapping : mappingsCopy) {
      CompoundNBT mappingNBT = new CompoundNBT();
      mappingNBT.putString("mapping", mapping.getRaw());
      mappingNBT.putInt("distributionScheme", mapping.getDistributionSchemeOrdinal());
      mappingNBT.putInt("filterScheme", mapping.getFilterSchemeOrdinal());

      ListNBT filterNBT = new ListNBT();
      for (int i = 0; i < mapping.filterInventory.getSizeInventory(); ++i) {
        if (!mapping.filterInventory.getStackInSlot(i).isEmpty()) {
          CompoundNBT compoundnbt = new CompoundNBT();
          compoundnbt.putByte("Slot", (byte) i);
          mapping.filterInventory.getStackInSlot(i).write(compoundnbt);
          filterNBT.add(compoundnbt);
        }
      }
      mappingNBT.put("filter", filterNBT);
      list.add(mappingNBT);
    }
    nbt.put("mappings", list);
    return nbt;
  }

  public static ArrayList<Mapping> getMappingsFromNBT(CompoundNBT tag, HashMap<String, BlockPos> identifiers, int filterSize) {
    ListNBT list = tag.getList("mappings", Constants.NBT.TAG_COMPOUND);
    ArrayList<Mapping> mappings = new ArrayList<>(list.size());
    for (int i = 0; i < list.size(); ++i) {
      CompoundNBT mappingNBT = list.getCompound(i);
      Mapping mapping = new Mapping(mappingNBT.getString("mapping"), identifiers.keySet(), Mapping.DistributionScheme.valueOf(mappingNBT.getInt("distributionScheme")), Mapping.FilterScheme.valueOf(mappingNBT.getInt("filterScheme")), filterSize);
      ListNBT filterItemsNBT = mappingNBT.getList("filter", Constants.NBT.TAG_COMPOUND);
      for(int j = 0; j < filterItemsNBT.size(); ++j) {
        CompoundNBT itemStackNBT = filterItemsNBT.getCompound(j);
        ItemStack is = ItemStack.read(itemStackNBT);
        mapping.getFilterInventory().setInventorySlotContents(j, is);
      }
      mappings.add(mapping);
    }
    return mappings;
  }

  public DistributionScheme getDistributionScheme() {
    return distributionScheme;
  }

  public void setDistributionScheme(DistributionScheme distributionScheme) {
    this.distributionScheme = distributionScheme;
  }

  public void setFilterScheme(FilterScheme filterScheme) {
    this.filterScheme = filterScheme;
  }

  public String getRaw() {
    return raw;
  }

  public ArrayList<NodeInfo> getInputs() {
    return inputs;
  }

  public ArrayList<NodeInfo> getOutputs() {
    return outputs;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public int getDistributionSchemeOrdinal() {
    if (distributionScheme == null) return -1;
    else return distributionScheme.ordinal();
  }

  public int getFilterSchemeOrdinal() {
    if (filterScheme == null) return -1;
    else return filterScheme.ordinal();
  }

  public FilterScheme getFilterScheme() {
    return filterScheme;
  }

  public enum DistributionScheme {
    NATURAL,
    RANDOM,
    CYCLIC;

    public static DistributionScheme valueOf(int ordinal) {
      if (ordinal == 0) return NATURAL;
      else if (ordinal == 1) return RANDOM;
      else if (ordinal == 2) return CYCLIC;
      else return null;
    }

  }

  public enum FilterScheme {
    BLACK_LIST,
    WHITE_LIST;

    public static FilterScheme valueOf(int ordinal) {
      if (ordinal == 0) return BLACK_LIST;
      else if (ordinal == 1) return WHITE_LIST;
      else return null;
    }

  }

  public Inventory getFilterInventory() {
    return filterInventory;
  }

}
