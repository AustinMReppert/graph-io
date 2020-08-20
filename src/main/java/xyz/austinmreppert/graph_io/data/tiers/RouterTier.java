package xyz.austinmreppert.graph_io.data.tiers;

public class RouterTier {

  public final Tier tier;
  public final int filterSize;
  public final int maxItemsPerTick;
  public final int maxBucketsPerTick;
  public final int maxEnergyPerTick;
  public final int maxEnergy;
  public final int minTickDelay;

  public RouterTier(Tier tier) {
    this.tier = tier;
    if (tier == Tier.BASIC) {
      maxItemsPerTick = 1;
      maxBucketsPerTick = 100;
      maxEnergyPerTick = 3200;
      minTickDelay = 20;
      maxEnergy = maxEnergyPerTick * 5;
      filterSize = 5;
    } else if (tier == Tier.ADVANCED) {
      maxItemsPerTick = 16;
      maxBucketsPerTick = 400;
      maxEnergyPerTick = 12800;
      minTickDelay = 15;
      maxEnergy = maxEnergyPerTick * 5;
      filterSize = 10;
    } else if (tier == Tier.ELITE) {
      maxItemsPerTick = 32;
      maxBucketsPerTick = 1600;
      maxEnergyPerTick = 64000;
      minTickDelay = 10;
      maxEnergy = maxEnergyPerTick * 5;
      filterSize = 15;
    } else if (tier == Tier.ULTIMATE) {
      maxItemsPerTick = 64;
      maxBucketsPerTick = 6400;
      maxEnergyPerTick = 320000;
      minTickDelay = 1;
      maxEnergy = maxEnergyPerTick * 5;
      filterSize = 20;
    } else {
      throw new IllegalArgumentException("Invalid tier.");
    }
  }

  public RouterTier(int tierOrdinal) {
    this(Tier.valueOf(tierOrdinal));
  }



}