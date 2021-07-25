package xyz.austinmreppert.graph_io.capabilities;

import net.minecraft.core.BlockPos;

public interface IIdentifierCapability {

  public BlockPos getBlockPos();
  public void setBlockPos(BlockPos blockPos);

}
