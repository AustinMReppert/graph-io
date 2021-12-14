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
    switch (baseTier) {
      case BASIC -> {
        maxItemsPerUpdate = 1;
        maxFluidPerUpdate = 250;
        maxEnergyPerUpdate = 3200;
        updateDelay = 20;
        maxEnergy = maxEnergyPerUpdate * 5;
        filterSize = 5;
      }
      case ADVANCED -> {
        maxItemsPerUpdate = 16;
        maxFluidPerUpdate = 500;
        maxEnergyPerUpdate = 12800;
        updateDelay = 15;
        maxEnergy = maxEnergyPerUpdate * 5;
        filterSize = 10;
      }
      case ELITE -> {
        maxItemsPerUpdate = 32;
        maxFluidPerUpdate = 750;
        maxEnergyPerUpdate = 64000;
        updateDelay = 10;
        maxEnergy = maxEnergyPerUpdate * 5;
        filterSize = 15;
      }
      case ULTIMATE -> {
        maxItemsPerUpdate = 64;
        maxFluidPerUpdate = 1000;
        maxEnergyPerUpdate = 320000;
        updateDelay = 1;
        maxEnergy = maxEnergyPerUpdate * 5;
        filterSize = 20;
      }
      default -> throw new IllegalArgumentException("Invalid tier.");
    }
  }
}