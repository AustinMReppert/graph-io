package com.austinmreppert.graphio.data;

public enum RedstoneMode {

  IGNORED,
  ACTIVE,
  INACTIVE;

  public static RedstoneMode valueOf(final int ordinal) {
    return switch (ordinal) {
      case 0 -> RedstoneMode.IGNORED;
      case 1 -> RedstoneMode.ACTIVE;
      case 2 -> RedstoneMode.INACTIVE;
      default -> throw new IllegalArgumentException("Unknown redstone mode ordinal.");
    };
  }

}
