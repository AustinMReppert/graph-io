package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.Set;

public class Mapping {

  private String raw;
  private ArrayList<NodeInfo> inputs;
  private ArrayList<NodeInfo> outputs;
  private boolean valid;

  public Mapping(String raw, Set<String> identifiers) {
    this.raw = raw;
    inputs = new ArrayList<>();
    outputs = new ArrayList<>();
    String[] components = raw.split("->");
    if (components.length == 2) {
      valid = true;
      String[] inputs = components[0].split(",");
      String[] outputs = components[1].split(",");
      for (String input : inputs) {
        ArrayList<NodeInfo> inputNodeInfos = NodeInfo.getNodeInfo(input, identifiers);
        for (NodeInfo inputNodeInfo : inputNodeInfos)
          if (!inputNodeInfo.isValid()) {
            valid = false;
            break;
          }
        this.inputs.addAll(inputNodeInfos);
      }
      for (String output : outputs) {
        ArrayList<NodeInfo> outputNodeInfos = NodeInfo.getNodeInfo(output, identifiers);
        for (NodeInfo outputNodeInfo : outputNodeInfos)
          if (!outputNodeInfo.isValid()) {
            valid = false;
            break;
          }
        this.outputs.addAll(outputNodeInfos);
      }
    }

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

}
