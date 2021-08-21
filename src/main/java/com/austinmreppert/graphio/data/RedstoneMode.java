package com.austinmreppert.graphio.data;

public enum RedstoneMode {

  IGNORED,
  ACTIVE,
  INACTIVE;

  public static RedstoneMode valueOf(final int ordinal) {
    switch (ordinal) {
      case 0:
        return RedstoneMode.IGNORED;
      case 1:
        return RedstoneMode.ACTIVE;
      case 2:
        return RedstoneMode.INACTIVE;
      default:
        throw new IllegalArgumentException("Unknown redstone mode ordinal.");
    }
  }

}
