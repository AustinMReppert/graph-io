package com.austinmreppert.graphio.capabilities;

import net.minecraft.util.Mth;
import net.minecraftforge.energy.EnergyStorage;

public class DynamicEnergyStorage extends EnergyStorage {

  public DynamicEnergyStorage(int capacity) {
    super(capacity);
  }

  public DynamicEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
    super(capacity, maxReceive, maxExtract, energy);
  }

  public void setEnergyStored(int amount, boolean clamp) {
    this.energy = clamp ? Mth.clamp(amount, 0, capacity) : amount;
  }

  public void setEnergyStored(int amount) {
    setEnergyStored(amount, true);
  }

  public void setCapacity(int capacity) {
    this.capacity = Math.max(0, capacity);
    setEnergyStored(energy);
  }

  public void setMaxReceive(int maxReceive) {
    this.maxReceive = maxReceive;
  }

  public void setMaxExtract(int maxExtract) {
    this.maxExtract = maxExtract;
  }

}
