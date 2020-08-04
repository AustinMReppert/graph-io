package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.ArrayList;
import java.util.Set;

public class Mapping {

  private String raw;
  private ArrayList<NodeInfo> inputs;
  private ArrayList<NodeInfo> outputs;
  private boolean valid;
  private DistributionScheme distributionScheme;
  public int currentInputIndex;
  public int currentOutputIndex;

  public Mapping(String raw, Set<String> identifiers, DistributionScheme distributionScheme) {
    this.raw = raw;
    this.distributionScheme = distributionScheme;
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

    System.out.println("Inputs: ");
    for(NodeInfo info : this.inputs)
      System.out.println("\t" + info.getIdentifier());

    System.out.println("Outputs: ");
    for(NodeInfo info : this.outputs)
      System.out.println("\t" + info.getIdentifier());
    valid = true;
  }

  public Mapping(String raw, DistributionScheme distributionScheme) {
    this.raw = raw;
    this.distributionScheme = distributionScheme;
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
      list.add(mappingNBT);
    }
    nbt.put("mappings", list);
    return nbt;
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
    if(distributionScheme == null) return -1;
    else return distributionScheme.ordinal();
  }

  public enum DistributionScheme {
    NATURAL,
    RANDOM,
    CYCLIC;

    public static DistributionScheme valueOf(int ordinal) {
      if(ordinal == 0) return NATURAL;
      else if(ordinal == 1) return RANDOM;
      else if(ordinal == 2) return CYCLIC;
      else return null;
    }

  }

}
