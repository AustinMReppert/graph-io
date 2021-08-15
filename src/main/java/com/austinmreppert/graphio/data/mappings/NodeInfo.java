package com.austinmreppert.graphio.data.mappings;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Set;

/**
 * Stores and parses information about a node in a mapping.
 */
public class NodeInfo {

  private String identifier;
  private Direction face;
  private boolean valid;

  private NodeInfo() {
  }

  public NodeInfo(final String identifier, final Direction face, final boolean valid) {
    this.identifier = identifier;
    this.face = face;
    this.valid = valid;
  }

  /**
   * Gets a list of nodes.
   *
   * @param raw         The raw representation of a node.
   * @param identifiers A set of identifiers that can be used while parsing the node.
   * @return A list of nodes.
   */
  public static ArrayList<NodeInfo> getNodeInfo(final String raw, final Set<String> identifiers) {
    final ArrayList<NodeInfo> nodeInfos = new ArrayList<>();
    if (raw.matches("((\\p{L}+\\*?)|\\*)(\\.\\p{L}+)?")) {
      final String[] nodeInfoComponents = raw.split("\\.");
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

  /**
   * Maps the nodes to a list of identifiers.
   *
   * @param raw         The name part of a node.
   * @param nodeInfos   The list of {@link NodeInfo}s to add parsed nodes to.
   * @param identifiers A set of valid identifiers.
   * @param face        The face of the node.
   */
  private static void getMatchingIdentifiers(final String raw, final ArrayList<NodeInfo> nodeInfos, final Set<String> identifiers, final Direction face) {
    if (raw.endsWith("*")) {
      final String prefix = raw.substring(0, Math.max(0, raw.length() - 1));
      for (final String identifier : identifiers)
        if (identifier.indexOf(prefix) == 0)
          nodeInfos.add(new NodeInfo(identifier, face, true));
    } else
      nodeInfos.add(new NodeInfo(raw, face, true));
  }

  /**
   * Gets the identifier of the node.
   *
   * @return The identifier of the node.
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * Gets the face of the node.
   *
   * @return The face of the node.
   */
  public Direction getFace() {
    return face;
  }

  /**
   * Gets whether the node is valid.
   *
   * @return Whether the node is valid.
   */
  public boolean isValid() {
    return valid;
  }

}
