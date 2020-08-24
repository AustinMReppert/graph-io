package xyz.austinmreppert.graph_io.data.tiers;

public enum BaseTier {

  INVALID,
  BASIC,
  ADVANCED,
  ELITE,
  ULTIMATE;

  public static BaseTier valueOf(int ordinal) {
    switch (ordinal) {
      case 0:
        return BaseTier.INVALID;
      case 1:
        return BaseTier.BASIC;
      case 2:
        return BaseTier.ADVANCED;
      case 3:
        return BaseTier.ELITE;
      case 4:
        return BaseTier.ULTIMATE;
      default:
        throw new IllegalArgumentException("Unknown tier ordinal.");
    }
  }

}
