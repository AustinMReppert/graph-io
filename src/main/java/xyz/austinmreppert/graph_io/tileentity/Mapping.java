package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
  private final Inventory filterInventory;
  public int currentInputIndex;
  public int currentOutputIndex;
  private int itemsPerTick;
  private int bucketsPerTick;
  private int energyPerTick;
  private int tickDelay;
  private int maxItemsPerTick;
  private int maxBucketsTick;
  private int maxEnergyPerTick;
  private int minTickDelay;
  private int lastTick;

  public Mapping(String raw, Set<String> identifiers, DistributionScheme distributionScheme, FilterScheme filterScheme, int filterSize, int itemsPerTick, int bucketsPerTick, int energyPerTick, int tickDelay, int maxItemsPerTick, int maxBucketsPerTick, int maxEnergyPerTick, int minTickDelay) {
    this.raw = raw;
    this.distributionScheme = distributionScheme;
    this.filterScheme = filterScheme;
    this.maxItemsPerTick = maxItemsPerTick;
    this.maxBucketsTick = maxBucketsPerTick;
    this.maxEnergyPerTick = maxEnergyPerTick;
    this.minTickDelay = minTickDelay;
    this.itemsPerTick = MathHelper.clamp(itemsPerTick, 0, maxItemsPerTick);
    this.bucketsPerTick = MathHelper.clamp(bucketsPerTick, 0, maxBucketsPerTick);
    this.energyPerTick = MathHelper.clamp(energyPerTick, 0, maxEnergyPerTick);
    this.tickDelay = MathHelper.clamp(tickDelay, minTickDelay, 20);
    filterInventory = new Inventory(filterSize);
    String[] components = raw.split("->");
    inputs = new ArrayList<>();
    outputs = new ArrayList<>();
    if (components.length != 2)
      return;
    String[] inputsTmp = components[0].split(",");
    String[] outputsTmp = components[1].split(",");
    ArrayList<String> inputs = new ArrayList<>();
    ArrayList<String> outputs = new ArrayList<>();

    // Only parse unique inputs/outputs
    for (String input : inputsTmp)
      if (!inputs.contains(input))
        inputs.add(input);
    for (String output : outputsTmp)
      if (!outputs.contains(output))
        outputs.add(output);

    // If any inputs or outputs are invalid, then the entire mapping is
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

  public Mapping(String raw, DistributionScheme distributionScheme, FilterScheme filterScheme, int filterSize, int itemsPerTick, int bucketsPerTick, int energyPerTick, int tickDelay, int maxItemsPerTick, int maxBucketsPerTick, int maxEnergyPerTick, int minTickDelay) {
    this.raw = raw;
    this.distributionScheme = distributionScheme;
    this.filterScheme = filterScheme;
    filterInventory = new Inventory(filterSize);
    this.maxItemsPerTick = maxItemsPerTick;
    this.maxBucketsTick = maxBucketsPerTick;
    this.maxEnergyPerTick = maxEnergyPerTick;
    this.minTickDelay = minTickDelay;
    this.itemsPerTick = MathHelper.clamp(itemsPerTick, 0, maxItemsPerTick);
    this.bucketsPerTick = MathHelper.clamp(bucketsPerTick, 0, maxBucketsPerTick);
    this.energyPerTick = MathHelper.clamp(energyPerTick, 0, maxEnergyPerTick);
    this.tickDelay = MathHelper.clamp(tickDelay, minTickDelay, 20);
  }

  public Mapping(Mapping mapping) {
    this(mapping.raw, mapping.distributionScheme, mapping.filterScheme, mapping.filterInventory.getSizeInventory(),
      mapping.itemsPerTick, mapping.bucketsPerTick, mapping.energyPerTick, mapping.tickDelay, mapping.maxItemsPerTick,
      mapping.maxBucketsTick, mapping.maxEnergyPerTick, mapping.minTickDelay);
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
      mappingNBT.putInt("itemsPerTick", mapping.itemsPerTick);
      mappingNBT.putInt("bucketsPerTick", mapping.bucketsPerTick);
      mappingNBT.putInt("energyPerTick", mapping.energyPerTick);

      ListNBT filterNBT = new ListNBT();
      for (int i = 0; i < mapping.filterInventory.getSizeInventory(); ++i) {
        if (!mapping.filterInventory.getStackInSlot(i).isEmpty()) {
          CompoundNBT compoundnbt = new CompoundNBT();
          compoundnbt.putByte("slot", (byte) i);
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

  public static ArrayList<Mapping> getMappingsFromNBT(CompoundNBT tag, HashMap<String, BlockPos> identifiers, int filterSize, int maxItemsPerTick, int maxBucketsPerTick, int maxEnergyPerTick, int minTickDelay) {
    ListNBT list = tag.getList("mappings", Constants.NBT.TAG_COMPOUND);
    ArrayList<Mapping> mappings = new ArrayList<>(list.size());
    for (int i = 0; i < list.size(); ++i) {
      CompoundNBT mappingNBT = list.getCompound(i);
      Mapping mapping = new Mapping(mappingNBT.getString("mapping"), identifiers.keySet(),
        Mapping.DistributionScheme.valueOf(mappingNBT.getInt("distributionScheme")),
        Mapping.FilterScheme.valueOf(mappingNBT.getInt("filterScheme")), filterSize,
        mappingNBT.getInt("itemsPerTick"),
        mappingNBT.getInt("bucketsPerTick"),
        mappingNBT.getInt("energyPerTick"), mappingNBT.getInt("tickDelay"), maxItemsPerTick, maxBucketsPerTick, maxEnergyPerTick, minTickDelay);
      ListNBT filterItemsNBT = mappingNBT.getList("filter", Constants.NBT.TAG_COMPOUND);
      for (int j = 0; j < filterItemsNBT.size(); ++j) {
        CompoundNBT itemStackNBT = filterItemsNBT.getCompound(j);
        int slot = itemStackNBT.getByte("slot");
        ItemStack is = ItemStack.read(itemStackNBT);
        mapping.getFilterInventory().setInventorySlotContents(slot, is);
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

  public boolean isValid() {
    return valid;
  }

  public void changeItemsPerTick(int amount) {
    itemsPerTick = MathHelper.clamp(itemsPerTick + amount, 0, maxItemsPerTick);
  }

  public void changeBucketsPerTick(int amount) {
    bucketsPerTick = MathHelper.clamp(bucketsPerTick + amount, 0, maxBucketsTick);
  }

  public void changeEnergyPerTick(int amount) {
    energyPerTick = MathHelper.clamp(energyPerTick + amount, 0, maxEnergyPerTick);
  }

  public boolean shouldTick(int ticks) {
    if (ticks - lastTick >= tickDelay) {
      lastTick = ticks;
      return true;
    }
    return false;
  }

  public void changeTickDelay(int amount) {
    tickDelay = MathHelper.clamp(tickDelay + amount, minTickDelay, 20);
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

  public int getItemsPerTick() {
    return itemsPerTick;
  }

  public int getBucketsPerTick() {
    return bucketsPerTick;
  }

  public int getEnergyPerTick() {
    return energyPerTick;
  }

  public int getTickDelay() {
    return tickDelay;
  }
}
