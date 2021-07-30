package xyz.austinmreppert.graph_io.data.mappings;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.Constants;
import xyz.austinmreppert.graph_io.capabilities.IIdentifierCapability;
import xyz.austinmreppert.graph_io.data.tiers.RouterTier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Mapping {

  private final SimpleContainer filterInventory;
  private final RouterTier routerTier;
  public int currentInputIndex;
  public int currentOutputIndex;
  private String raw;
  private ArrayList<NodeInfo> inputs;
  private ArrayList<NodeInfo> outputs;
  private boolean valid;
  private DistributionScheme distributionScheme;
  private FilterScheme filterScheme;
  private int itemsPerTick;
  private int bucketsPerTick;
  private int energyPerTick;
  private int tickDelay;
  private int lastTick;

  public Mapping(String raw, Set<String> identifiers, DistributionScheme distributionScheme, FilterScheme filterScheme, int itemsPerTick, int bucketsPerTick, int energyPerTick, int tickDelay, RouterTier tier) {
    this(raw, distributionScheme, filterScheme, itemsPerTick, bucketsPerTick, energyPerTick, tickDelay, tier);
    String[] components = raw.split("->");
    inputs = new ArrayList<>();
    outputs = new ArrayList<>();
    if (components.length != 2)
      return;
    parseMapping(identifiers, components);
  }

  private void parseMapping(Set<String> identifiers, String[] components) {
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

  public Mapping(String raw, DistributionScheme distributionScheme, FilterScheme filterScheme, int itemsPerTick, int bucketsPerTick, int energyPerTick, int tickDelay, RouterTier tier) {
    this.raw = raw;
    this.distributionScheme = distributionScheme;
    this.filterScheme = filterScheme;
    this.routerTier = tier;
    filterInventory = new SimpleContainer(tier.filterSize);
    this.itemsPerTick = Mth.clamp(itemsPerTick, 0, tier.maxItemsPerTick);
    this.bucketsPerTick = Mth.clamp(bucketsPerTick, 0, tier.maxBucketsPerTick);
    this.energyPerTick = Mth.clamp(energyPerTick, 0, tier.maxEnergyPerTick);
    this.tickDelay = Mth.clamp(tickDelay, tier.minTickDelay, 20);
  }

  public Mapping(String raw, DistributionScheme distributionScheme, FilterScheme filterScheme, RouterTier tier) {
    this(raw, distributionScheme, filterScheme, tier.maxItemsPerTick, tier.maxBucketsPerTick, tier.maxEnergyPerTick, tier.minTickDelay, tier);
  }

  public static CompoundTag write(ArrayList<Mapping> mappingsCopy) {
    CompoundTag mappingsNBT = new CompoundTag();
    return Mapping.write(mappingsCopy, mappingsNBT);
  }

  public static CompoundTag write(ArrayList<Mapping> mappingsCopy, CompoundTag nbt) {
    ListTag list = new ListTag();
    for (Mapping mapping : mappingsCopy) {
      CompoundTag mappingNBT = new CompoundTag();
      mappingNBT.putString("mapping", mapping.getRaw());
      mappingNBT.putInt("distributionScheme", mapping.getDistributionSchemeOrdinal());
      mappingNBT.putInt("filterScheme", mapping.getFilterSchemeOrdinal());
      mappingNBT.putInt("itemsPerTick", mapping.itemsPerTick);
      mappingNBT.putInt("bucketsPerTick", mapping.bucketsPerTick);
      mappingNBT.putInt("energyPerTick", mapping.energyPerTick);
      mappingNBT.putInt("tickDelay", mapping.tickDelay);

      ListTag filterNBT = new ListTag();
      mapping.filterInventory.createTag();
      for (int i = 0; i < mapping.filterInventory.getContainerSize(); ++i) {
        if (!mapping.filterInventory.getItem(i).isEmpty()) {
          CompoundTag CompoundTag = new CompoundTag();
          CompoundTag.putByte("slot", (byte) i);
          mapping.filterInventory.getItem(i).save(CompoundTag);
          filterNBT.add(CompoundTag);
        }
      }
      mappingNBT.put("filter", filterNBT);
      list.add(mappingNBT);
    }
    nbt.put("mappings", list);
    return nbt;
  }

  public static ArrayList<Mapping> read(CompoundTag tag, HashMap<String, IIdentifierCapability> identifiers, RouterTier tier) {
    ListTag list = tag.getList("mappings", Constants.NBT.TAG_COMPOUND);
    ArrayList<Mapping> mappings = new ArrayList<>(list.size());
    for (int i = 0; i < list.size(); ++i) {
      CompoundTag mappingNBT = list.getCompound(i);
      Mapping mapping = new Mapping(mappingNBT.getString("mapping"), identifiers.keySet(),
        Mapping.DistributionScheme.valueOf(mappingNBT.getInt("distributionScheme")),
        Mapping.FilterScheme.valueOf(mappingNBT.getInt("filterScheme")),
        mappingNBT.getInt("itemsPerTick"),
        mappingNBT.getInt("bucketsPerTick"),
        mappingNBT.getInt("energyPerTick"), mappingNBT.getInt("tickDelay"), tier);
      ListTag filterItemsNBT = mappingNBT.getList("filter", Constants.NBT.TAG_COMPOUND);
      for (int j = 0; j < filterItemsNBT.size(); ++j) {
        CompoundTag itemStackNBT = filterItemsNBT.getCompound(j);
        int slot = itemStackNBT.getByte("slot");
        ItemStack is = ItemStack.of(itemStackNBT);
        mapping.getFilterInventory().setItem(slot, is);
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

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public ArrayList<NodeInfo> getInputs() {
    return inputs;
  }

  public ArrayList<NodeInfo> getOutputs() {
    return outputs;
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

  public void setFilterScheme(FilterScheme filterScheme) {
    this.filterScheme = filterScheme;
  }

  public boolean isValid() {
    return valid;
  }

  public void changeItemsPerTick(int amount) {
    itemsPerTick = Mth.clamp(itemsPerTick + amount, 0, routerTier.maxItemsPerTick);
  }

  public void changeBucketsPerTick(int amount) {
    bucketsPerTick = Mth.clamp(bucketsPerTick + amount, 0, routerTier.maxBucketsPerTick);
  }

  public void changeEnergyPerTick(int amount) {
    energyPerTick = Mth.clamp(energyPerTick + amount, 0, routerTier.maxEnergyPerTick);
  }

  public boolean shouldTick(int ticks) {
    if (ticks - lastTick >= tickDelay) {
      lastTick = ticks;
      return true;
    }
    return false;
  }

  public void changeTickDelay(int amount) {
    tickDelay = Mth.clamp(tickDelay + amount, routerTier.minTickDelay, 20);
  }

  public SimpleContainer getFilterInventory() {
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
}
