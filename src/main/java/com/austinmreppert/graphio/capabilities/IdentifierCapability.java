package com.austinmreppert.graphio.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class IdentifierCapability implements IIdentifierCapability {

  private BlockPos blockPos;
  private ResourceKey<Level> level;

  @Override
  public BlockPos getBlockPos() {
    return blockPos;
  }

  @Override
  public ResourceKey<Level> getLevel() {
    return level;
  }

  @Override
  public void setBlockPos(BlockPos blockPos) {
    this.blockPos = blockPos;
  }

  @Override
  public void setLevel(ResourceKey<Level> level) {
    this.level = level;
  }

}
