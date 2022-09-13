package com.austinmreppert.graphio.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

public interface IIdentifierCapability extends INBTSerializable<CompoundTag> {

  /**
   * Gets the level of the stored {@link BlockPos}.
   *
   * @return The level of the stored {@link BlockPos}.
   */
  ResourceKey<Level> getLevel();

  /**
   * Gets the stored block pos.
   *
   * @return The stored block pos.
   */
  BlockPos getBlockPos();

  /**
   * Sets the stored block position.
   *
   * @param blockPos The stored block position.
   */
  void setBlockPos(BlockPos blockPos);

  /**
   * Sets the level of the stored {@link BlockPos}.
   *
   * @param level The level of the stored {@link BlockPos}.
   */
  void setLevel(ResourceKey<Level> level);

}
