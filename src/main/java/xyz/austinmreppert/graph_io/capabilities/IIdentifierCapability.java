package xyz.austinmreppert.graph_io.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IIdentifierCapability {

  public ResourceKey<Level> getLevel();
  public BlockPos getBlockPos();
  public void setBlockPos(BlockPos blockPos);
  public void setLevel(ResourceKey<Level> level);

}
