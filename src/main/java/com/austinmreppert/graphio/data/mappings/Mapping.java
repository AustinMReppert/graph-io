package com.austinmreppert.graphio.data.mappings;

import com.austinmreppert.graphio.capabilities.IIdentifierCapability;
import com.austinmreppert.graphio.data.tiers.RouterTier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Stores and parses all the data needed to transfer items via a {@link com.austinmreppert.graphio.blockentity.RouterBlockEntity}.
 */
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
  private int itemsPerUpdate;
  private int fluidPerUpdate;
  private int energyPerUpdate;
  private int updateDelay;
  private int lastTick;

  public Mapping(final String raw, final Set<String> identifiers, final DistributionScheme distributionScheme,
                 final FilterScheme filterScheme, final int itemsPerUpdate, final int fluidPerUpdate,
                 final int energyPerTick, final int updateDelay, final RouterTier tier) {
    this(raw, distributionScheme, filterScheme, itemsPerUpdate, fluidPerUpdate, energyPerTick, updateDelay, tier);
    String[] components = raw.split("->");
    inputs = new ArrayList<>();
    outputs = new ArrayList<>();
    parseMapping(identifiers, components);
  }

  // TODO: Implement.
  public static boolean equals(ArrayList<Mapping> mappings, ArrayList<Mapping> mappings1) {
    if(mappings.size() == mappings1.size()) {
      for(int i = 0; i < mappings.size(); ++i) {
        if(!mappings.get(i).raw.equals(mappings1.get(i).raw))
          return false;
      }
      return true;
    }
    return false;
  }

  /**
   * Parses the raw mappings string for inputs and outputs.
   *
   * @param identifiers The set of identifiers that can be used in the mapping.
   * @param components  An {@link String[]} of size 2 that stores the inputs as the first element and the outputs as the 2nd element.
   */
  private void parseMapping(Set<String> identifiers, String[] components) {
    if (components.length != 2)
      return;
    final String[] inputsTmp = components[0].split(",");
    final String[] outputsTmp = components[1].split(",");
    final ArrayList<String> inputs = new ArrayList<>();
    final ArrayList<String> outputs = new ArrayList<>();

    // Only parse unique inputs/outputs
    for (final String input : inputsTmp)
      if (!inputs.contains(input))
        inputs.add(input);
    for (final String output : outputsTmp)
      if (!outputs.contains(output))
        outputs.add(output);

    // If any inputs or outputs are invalid, then the entire mapping is
    for (final String input : inputs) {
      final ArrayList<NodeInfo> tmp = NodeInfo.getNodeInfo(input, identifiers);
      for (final NodeInfo inputNodeInfo : tmp)
        if (!inputNodeInfo.isValid())
          return;
      this.inputs.addAll(tmp);
    }
    for (final String output : outputs) {
      final ArrayList<NodeInfo> tmp = NodeInfo.getNodeInfo(output, identifiers);
      for (final NodeInfo outputNodeInfo : tmp)
        if (!outputNodeInfo.isValid())
          return;
      this.outputs.addAll(tmp);
    }

    valid = true;
  }

  public Mapping(final String raw, final DistributionScheme distributionScheme, final FilterScheme filterScheme,
                 final int itemsPerTick, final int fluidPerTick, final int energyPerTick, final int updateDelay, final RouterTier tier) {
    this.raw = raw;
    this.distributionScheme = distributionScheme;
    this.filterScheme = filterScheme;
    this.routerTier = tier;
    filterInventory = new SimpleContainer(tier.filterSize);
    this.itemsPerUpdate = Mth.clamp(itemsPerTick, 0, tier.maxItemsPerUpdate);
    this.fluidPerUpdate = Mth.clamp(fluidPerTick, 0, tier.maxFluidPerUpdate);
    this.energyPerUpdate = Mth.clamp(energyPerTick, 0, tier.maxEnergyPerUpdate);
    this.updateDelay = Mth.clamp(updateDelay, tier.updateDelay, 20);
  }

  public Mapping(final String raw, final DistributionScheme distributionScheme, final FilterScheme filterScheme, final RouterTier tier) {
    this(raw, distributionScheme, filterScheme, tier.maxItemsPerUpdate, tier.maxFluidPerUpdate, tier.maxEnergyPerUpdate, tier.updateDelay, tier);
  }

  /**
   * Writes a list of {@link Mapping}s into an {@link CompoundTag}.
   *
   * @param mappingsCopy The list of mappings to use.
   * @return A list of {@link Mapping}s as an {@link CompoundTag}.
   */
  public static CompoundTag write(final ArrayList<Mapping> mappingsCopy) {
    final var mappingsNBT = new CompoundTag();
    return Mapping.write(mappingsCopy, mappingsNBT);
  }

  /**
   * Writes a list of {@link Mapping}s into an {@link CompoundTag}.
   *
   * @param mappingsCopy The list of mappings to use.
   * @param nbt          The tag to use.
   * @return A list of {@link Mapping}s stored in {@code nbt}.
   */
  public static CompoundTag write(ArrayList<Mapping> mappingsCopy, CompoundTag nbt) {
    final var list = new ListTag();
    for (final Mapping mapping : mappingsCopy) {
      final var mappingNBT = new CompoundTag();
      mappingNBT.putString("mapping", mapping.getRaw());
      mappingNBT.putInt("distributionScheme", mapping.getDistributionSchemeOrdinal());
      mappingNBT.putInt("filterScheme", mapping.getFilterSchemeOrdinal());
      mappingNBT.putInt("itemsPerUpdate", mapping.itemsPerUpdate);
      mappingNBT.putInt("fluidPerUpdate", mapping.fluidPerUpdate);
      mappingNBT.putInt("energyPerUpdate", mapping.energyPerUpdate);
      mappingNBT.putInt("updateDelay", mapping.updateDelay);

      final var filterNBT = new ListTag();
      mapping.filterInventory.createTag();
      for (int i = 0; i < mapping.filterInventory.getContainerSize(); ++i) {
        if (!mapping.filterInventory.getItem(i).isEmpty()) {
          final var filterSlotNBT = new CompoundTag();
          filterSlotNBT.putByte("slot", (byte) i);
          mapping.filterInventory.getItem(i).save(filterSlotNBT);
          filterNBT.add(filterSlotNBT);
        }
      }
      mappingNBT.put("filter", filterNBT);
      list.add(mappingNBT);
    }
    nbt.put("mappings", list);
    return nbt;
  }

  /**
   * Reads a list of mappings from a {@link CompoundTag}.
   *
   * @param tag         The tag to read from.
   * @param identifiers A Map of identifier names and {@link com.austinmreppert.graphio.capabilities.IdentifierCapability}s.
   * @param tier        The tier of router the mapping is stored in
   * @return A list of {@link Mapping}s.
   */
  public static ArrayList<Mapping> read(final CompoundTag tag, final HashMap<String, IIdentifierCapability> identifiers, final RouterTier tier) {
    final var list = tag.getList("mappings", Constants.NBT.TAG_COMPOUND);
    final var mappings = new ArrayList<Mapping>(list.size());
    for (int i = 0; i < list.size(); ++i) {
      final CompoundTag mappingNBT = list.getCompound(i);
      final var mapping = new Mapping(mappingNBT.getString("mapping"), identifiers.keySet(),
          Mapping.DistributionScheme.valueOf(mappingNBT.getInt("distributionScheme")),
          Mapping.FilterScheme.valueOf(mappingNBT.getInt("filterScheme")),
          mappingNBT.getInt("itemsPerUpdate"),
          mappingNBT.getInt("fluidPerUpdate"),
          mappingNBT.getInt("energyPerUpdate"), mappingNBT.getInt("updateDelay"), tier);
      final var filterItemsNBT = mappingNBT.getList("filter", Constants.NBT.TAG_COMPOUND);
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

  /**
   * Gets the distribution scheme.
   *
   * @return The distribution scheme.
   */
  public DistributionScheme getDistributionScheme() {
    return distributionScheme;
  }

  /**
   * Sets the distribution scheme.
   *
   * @param distributionScheme A distribution scheme.
   */
  public void setDistributionScheme(final DistributionScheme distributionScheme) {
    this.distributionScheme = distributionScheme;
  }

  /**
   * Gets the raw string that the mapping was created from.
   *
   * @return The raw string that the mapping was created from.
   */
  public String getRaw() {
    return raw;
  }

  /**
   * Sets the raw string that the mapping was created from. Does not cause a re-parse.
   *
   * @param raw A mapping string.
   */
  public void setRaw(String raw) {
    this.raw = raw;
  }

  /**
   * Gets the list of inputs.
   *
   * @return The list of inputs.
   */
  public ArrayList<NodeInfo> getInputs() {
    return inputs;
  }

  /**
   * Gets the list of outputs.
   *
   * @return The list of outputs.
   */
  public ArrayList<NodeInfo> getOutputs() {
    return outputs;
  }

  /**
   * Gets an integer representation of the {Code distributionScheme}.
   *
   * @return An integer representation of the {Code distributionScheme}.
   */
  public int getDistributionSchemeOrdinal() {
    if (distributionScheme == null) return -1;
    else return distributionScheme.ordinal();
  }

  /**
   * Gets an integer representation of the {@code filterScheme}.
   *
   * @return An integer representation of the {@code filterScheme}.
   */
  public int getFilterSchemeOrdinal() {
    if (filterScheme == null) return -1;
    else return filterScheme.ordinal();
  }

  /**
   * Gets the filter scheme.
   *
   * @return The filter scheme.
   */
  public FilterScheme getFilterScheme() {
    return filterScheme;
  }

  /**
   * Sets the filter scheme.
   *
   * @param filterScheme The filter scheme.
   */
  public void setFilterScheme(final FilterScheme filterScheme) {
    this.filterScheme = filterScheme;
  }

  /**
   * Gets whether the mapping is valid.
   *
   * @return Whether the mapping is valid.
   */
  public boolean isValid() {
    return valid;
  }

  /**
   * Changes the amount of items that are transferred per an update.
   *
   * @param amount The amount of change in items.
   */
  public void changeItemsPerTick(final int amount) {
    itemsPerUpdate = Mth.clamp(itemsPerUpdate + amount, 0, routerTier.maxItemsPerUpdate);
  }

  /**
   * Changes the amount of fluid in millibuckets that is transferred per an update.
   *
   * @param amount The amount of change in millibuckets.
   */
  public void changeFluidPerUpdate(final int amount) {
    fluidPerUpdate = Mth.clamp(fluidPerUpdate + amount, 0, routerTier.maxFluidPerUpdate);
  }

  /**
   * Changes the amount of energy that is transferred per an update.
   *
   * @param amount The amount of change in energy.
   */
  public void changeEnergyPerUpdate(final int amount) {
    energyPerUpdate = Mth.clamp(energyPerUpdate + amount, 0, routerTier.maxEnergyPerUpdate);
  }

  /**
   * Determines whether the mapping should tick.
   *
   * @param ticks The current amount of ticks.
   * @return Whether the mapping should tick.
   */
  public boolean shouldUpdate(final int ticks) {
    if (ticks - lastTick >= updateDelay) {
      lastTick = ticks;
      return true;
    }
    return false;
  }

  /**
   * Changes the amount of ticks between updates.
   *
   * @param amount The amount of change in ticks.
   */
  public void changeUpdateDelay(final int amount) {
    updateDelay = Mth.clamp(updateDelay + amount, routerTier.updateDelay, 20);
  }

  /**
   * Gets the filter inventory.
   *
   * @return The filter inventory.
   */
  public SimpleContainer getFilterInventory() {
    return filterInventory;
  }

  /**
   * Gets the amount of items transferred per an update.
   *
   * @return The amount of items transferred per a tick.
   */
  public int getItemsPerUpdate() {
    return itemsPerUpdate;
  }

  /**
   * Gets the amount of fluid in millibuckets transferred per an update.
   *
   * @return The amount of millibuckets transferred per a tick.
   */
  public int getFluidPerUpdate() {
    return fluidPerUpdate;
  }

  /**
   * Gets the amount of energy transferred per an update.
   *
   * @return The amount of energy transferred per an update.
   */
  public int getEnergyPerUpdate() {
    return energyPerUpdate;
  }

  /**
   * Gets the amount of ticks between updates.
   *
   * @return The amount of ticks between updates.
   */
  public int getUpdateDelay() {
    return updateDelay;
  }

  /**
   * The types of distribution schemes.
   */
  public enum DistributionScheme {
    NATURAL,
    RANDOM,
    CYCLIC;

    public static DistributionScheme valueOf(final int ordinal) {
      if (ordinal == 0) return NATURAL;
      else if (ordinal == 1) return RANDOM;
      else if (ordinal == 2) return CYCLIC;
      else return null;
    }

  }

  /**
   * The types of filter schemes.
   */
  public enum FilterScheme {
    BLACK_LIST,
    WHITE_LIST;

    public static FilterScheme valueOf(final int ordinal) {
      if (ordinal == 0) return BLACK_LIST;
      else if (ordinal == 1) return WHITE_LIST;
      else return null;
    }

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Mapping mapping = (Mapping) o;
    return itemsPerUpdate == mapping.itemsPerUpdate && fluidPerUpdate == mapping.fluidPerUpdate && energyPerUpdate == mapping.energyPerUpdate && updateDelay == mapping.updateDelay && Objects.equals(filterInventory, mapping.filterInventory) && Objects.equals(raw, mapping.raw) && distributionScheme == mapping.distributionScheme && filterScheme == mapping.filterScheme;
  }

  @Override
  public int hashCode() {
    return Objects.hash(filterInventory, raw, distributionScheme, filterScheme, itemsPerUpdate, fluidPerUpdate, energyPerUpdate, updateDelay);
  }
}
