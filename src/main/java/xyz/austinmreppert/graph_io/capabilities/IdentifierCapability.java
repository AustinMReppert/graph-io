package xyz.austinmreppert.graph_io.capabilities;

import net.minecraft.util.math.BlockPos;

public class IdentifierCapability implements IIdentifierCapability {

  private BlockPos blockPos;

  @Override
  public BlockPos getBlockPos() {
    return blockPos;
  }

  @Override
  public void setBlockPos(BlockPos blockPos) {
    this.blockPos = blockPos;
  }

}
