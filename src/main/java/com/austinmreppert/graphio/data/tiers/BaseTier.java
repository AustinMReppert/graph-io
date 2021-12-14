package com.austinmreppert.graphio.data.tiers;

public enum BaseTier {

  INVALID,
  BASIC,
  ADVANCED,
  ELITE,
  ULTIMATE;

  public static BaseTier valueOf(final int ordinal) {
    return switch (ordinal) {
      case 0 -> BaseTier.INVALID;
      case 1 -> BaseTier.BASIC;
      case 2 -> BaseTier.ADVANCED;
      case 3 -> BaseTier.ELITE;
      case 4 -> BaseTier.ULTIMATE;
      default -> throw new IllegalArgumentException("Unknown tier ordinal.");
    };
  }

}
