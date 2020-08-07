package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.util.Direction;

import java.util.ArrayList;
import java.util.Set;

public class NodeInfo {

  private String identifier;
  private Direction face;
  private boolean valid;

  private NodeInfo() {
  }

  public NodeInfo(String identifier, Direction face, boolean valid) {
    this.identifier = identifier;
    this.face = face;
    this.valid = valid;
  }

  public static ArrayList<NodeInfo> getNodeInfo(String raw, Set<String> identifiers) {
    ArrayList<NodeInfo> nodeInfos = new ArrayList<>();
    if (raw.matches("((\\p{L}+\\*?)|\\*)(\\.\\p{L}+)?")) {
      String[] nodeInfoComponents = raw.split("\\.");
      if (nodeInfoComponents.length == 2) {
        Direction face = null;
        if (nodeInfoComponents[1].equals("north")) face = Direction.NORTH;
        else if (nodeInfoComponents[1].equals("east")) face = Direction.EAST;
        else if (nodeInfoComponents[1].equals("south")) face = Direction.SOUTH;
        else if (nodeInfoComponents[1].equals("west")) face = Direction.WEST;
        else if (nodeInfoComponents[1].equals("up") || nodeInfoComponents[1].equals("top")) face = Direction.UP;
        else if (nodeInfoComponents[1].equals("down") || nodeInfoComponents[1].equals("bottom")) face = Direction.DOWN;
        if (face != null)
          getMatchingIdentifiers(nodeInfoComponents[0], nodeInfos, identifiers, face);
        else nodeInfos.add(new NodeInfo(nodeInfoComponents[0], null, false));
      } else if (nodeInfoComponents.length == 1)
        getMatchingIdentifiers(nodeInfoComponents[0], nodeInfos, identifiers, null);
    } else
      nodeInfos.add(new NodeInfo(raw, null, false));
    return nodeInfos;
  }

  private static void getMatchingIdentifiers(String raw, ArrayList<NodeInfo> nodeInfos, Set<String> identifiers, Direction face) {
    if (raw.endsWith("*")) {
      String prefix = raw.substring(0, Math.max(0, raw.length() - 1));
      for (String identifier : identifiers)
        if (identifier.indexOf(prefix) == 0)
          nodeInfos.add(new NodeInfo(identifier, face, true));
    } else
      nodeInfos.add(new NodeInfo(raw, face, true));
  }

  public String getIdentifier() {
    return identifier;
  }

  public Direction getFace() {
    return face;
  }

  public boolean isValid() {
    return valid;
  }

}
