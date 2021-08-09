package com.austinmreppert.graphio.data.tiers;

import javax.annotation.Nonnull;

public class RouterTier {

  public final BaseTier baseTier;
  public final int filterSize;
  public final int maxItemsPerTick;
  public final int maxBucketsPerTick;
  public final int maxEnergyPerTick;
  public final int maxEnergy;
  public final int minTickDelay;

  public RouterTier(@Nonnull BaseTier baseTier) {
    this.baseTier = baseTier;
    if (baseTier == BaseTier.BASIC) {
      maxItemsPerTick = 1;
      maxBucketsPerTick = 100;
      maxEnergyPerTick = 3200;
      minTickDelay = 20;
      maxEnergy = maxEnergyPerTick * 5;
      filterSize = 5;
    } else if (baseTier == BaseTier.ADVANCED) {
      maxItemsPerTick = 16;
      maxBucketsPerTick = 400;
      maxEnergyPerTick = 12800;
      minTickDelay = 15;
      maxEnergy = maxEnergyPerTick * 5;
      filterSize = 10;
    } else if (baseTier == BaseTier.ELITE) {
      maxItemsPerTick = 32;
      maxBucketsPerTick = 1600;
      maxEnergyPerTick = 64000;
      minTickDelay = 10;
      maxEnergy = maxEnergyPerTick * 5;
      filterSize = 15;
    } else if (baseTier == BaseTier.ULTIMATE) {
      maxItemsPerTick = 64;
      maxBucketsPerTick = 6400;
      maxEnergyPerTick = 320000;
      minTickDelay = 1;
      maxEnergy = maxEnergyPerTick * 5;
      filterSize = 20;
    } else
      throw new IllegalArgumentException("Invalid tier.");
  }

}