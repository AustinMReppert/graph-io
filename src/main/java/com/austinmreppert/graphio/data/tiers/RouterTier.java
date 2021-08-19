package com.austinmreppert.graphio.data.tiers;

import javax.annotation.Nonnull;

public class RouterTier {

  public final BaseTier baseTier;
  public final int filterSize;
  public final int maxItemsPerUpdate;
  public final int maxFluidPerUpdate;
  public final int maxEnergyPerUpdate;
  public final int maxEnergy;
  public final int updateDelay;

  public RouterTier(@Nonnull BaseTier baseTier) {
    this.baseTier = baseTier;
    if (baseTier == BaseTier.BASIC) {
      maxItemsPerUpdate = 1;
      maxFluidPerUpdate = 250;
      maxEnergyPerUpdate = 3200;
      updateDelay = 20;
      maxEnergy = maxEnergyPerUpdate * 5;
      filterSize = 5;
    } else if (baseTier == BaseTier.ADVANCED) {
      maxItemsPerUpdate = 16;
      maxFluidPerUpdate = 500;
      maxEnergyPerUpdate = 12800;
      updateDelay = 15;
      maxEnergy = maxEnergyPerUpdate * 5;
      filterSize = 10;
    } else if (baseTier == BaseTier.ELITE) {
      maxItemsPerUpdate = 32;
      maxFluidPerUpdate = 750;
      maxEnergyPerUpdate = 64000;
      updateDelay = 10;
      maxEnergy = maxEnergyPerUpdate * 5;
      filterSize = 15;
    } else if (baseTier == BaseTier.ULTIMATE) {
      maxItemsPerUpdate = 64;
      maxFluidPerUpdate = 1000;
      maxEnergyPerUpdate = 320000;
      updateDelay = 1;
      maxEnergy = maxEnergyPerUpdate * 5;
      filterSize = 20;
    } else
      throw new IllegalArgumentException("Invalid tier.");
  }

}