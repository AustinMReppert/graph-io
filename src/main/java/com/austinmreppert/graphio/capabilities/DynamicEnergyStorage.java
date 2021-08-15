package com.austinmreppert.graphio.capabilities;

import net.minecraft.util.Mth;
import net.minecraftforge.energy.EnergyStorage;

/**
 * Stores energy. Allows resizing.
 */
public class DynamicEnergyStorage extends EnergyStorage {

  public DynamicEnergyStorage(int capacity) {
    super(capacity);
  }

  public DynamicEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
    super(capacity, maxReceive, maxExtract, energy);
  }

  /**
   * Sets the amount of energy.
   *
   * @param amount The amount of energy
   * @param clamp  Whether the amount should be clamped within the container's limits.
   */
  public void setEnergyStored(int amount, boolean clamp) {
    this.energy = clamp ? Mth.clamp(amount, 0, capacity) : amount;
  }

  /**
   * Sets the amount of energy.
   *
   * @param amount The amount of energy.
   */
  public void setEnergyStored(int amount) {
    setEnergyStored(amount, true);
  }

  /**
   * Sets the maximum energy that can be stored.
   *
   * @param capacity The maximum energy that can be stored.
   */
  public void setCapacity(int capacity) {
    this.capacity = Math.max(0, capacity);
    setEnergyStored(energy);
  }

  /**
   * Sets the maximum energy that can be received.
   *
   * @param maxReceive The maximum energy that can be received.
   */
  public void setMaxReceive(int maxReceive) {
    this.maxReceive = maxReceive;
  }

  /**
   * Sets the maximum energy that can be extracted.
   *
   * @param maxExtract The maximum energy that can be extracted.
   */
  public void setMaxExtract(int maxExtract) {
    this.maxExtract = maxExtract;
  }

}
