package com.austinmreppert.graphio.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

  @Override
  public CompoundTag serializeNBT() {
    final var tag = new CompoundTag();
    if (blockPos != null) {
      tag.putInt("x", blockPos.getX());
      tag.putInt("y", blockPos.getY());
      tag.putInt("z", blockPos.getZ());
    }
    if (level != null)
      tag.putString("levelLocation", level.location().toString());
    return tag;
  }

  @Override
  public void deserializeNBT(final CompoundTag tag) {
    if (tag.contains("x") && tag.contains("y") && tag.contains("z"))
      setBlockPos(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
    if (tag.contains("levelLocation"))
      setLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("levelLocation"))));
  }
}
