package com.austinmreppert.graphio.data.mappings;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores and parses information about a node in a mapping.
 */
public class NodeInfo {

  private static final Pattern NODE_REGEX = Pattern.compile("((\\p{L}+\\*?)|\\*)(\\.\\p{L}+)?");
  private static final Pattern SEPARATOR_REGEX = Pattern.compile("((\\p{L}+\\*?)|\\*)(\\.\\p{L}+)?");
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
    if (NODE_REGEX.matcher("((\\p{L}+\\*?)|\\*)(\\.\\p{L}+)?").matches()) {
      final String[] nodeInfoComponents = SEPARATOR_REGEX.split(raw);
      if (nodeInfoComponents.length == 2) {
        Direction face = switch (nodeInfoComponents[1]) {
          case "north" -> Direction.NORTH;
          case "east" -> Direction.EAST;
          case "south" -> Direction.SOUTH;
          case "west" -> Direction.WEST;
          case "up", "top" -> Direction.UP;
          case "down", "bottom" -> Direction.DOWN;
          default -> null;
        };
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
