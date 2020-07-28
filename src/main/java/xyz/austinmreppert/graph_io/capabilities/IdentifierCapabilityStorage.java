package xyz.austinmreppert.graph_io.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class IdentifierCapabilityStorage implements Capability.IStorage<IIdentifierCapability> {

  @Nullable
  @Override
  public INBT writeNBT(Capability<IIdentifierCapability> capability, IIdentifierCapability instance, Direction side) {
    CompoundNBT blockPosNBT = new CompoundNBT();
    if (instance.getBlockPos() != null) {
      blockPosNBT.putInt("x", instance.getBlockPos().getX());
      blockPosNBT.putInt("y", instance.getBlockPos().getY());
      blockPosNBT.putInt("z", instance.getBlockPos().getZ());
    }
    return blockPosNBT;
  }

  @Override
  public void readNBT(Capability<IIdentifierCapability> capability, IIdentifierCapability instance, Direction side, INBT nbt) {
    CompoundNBT blockPosNBT = (CompoundNBT) nbt;
    if (blockPosNBT.contains("x") && blockPosNBT.contains("y") && blockPosNBT.contains("z"))
      instance.setBlockPos(new BlockPos(blockPosNBT.getInt("x"), blockPosNBT.getInt("y"), blockPosNBT.getInt("z")));
  }

}
