package xyz.austinmreppert.graph_io.data.tiers;

public enum Tier {

  BASIC,
  ADVANCED,
  ELITE,
  ULTIMATE;

  public static Tier valueOf(int ordinal) {
    if (ordinal == 0) return Tier.BASIC;
    else if (ordinal == 1) return Tier.ADVANCED;
    else if (ordinal == 2) return Tier.ELITE;
    else if (ordinal == 3) return Tier.ULTIMATE;
    else return null;
  }

  public static int getOrdinal(Tier tier) {
    if(tier != null) return tier.ordinal();
    return -1;
  }

}
