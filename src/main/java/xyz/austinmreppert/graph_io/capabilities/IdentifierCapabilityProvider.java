package xyz.austinmreppert.graph_io.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IdentifierCapabilityProvider implements ICapabilitySerializable {

  private final LazyOptional<IIdentifierCapability> identifierCapabilityLO = LazyOptional.of(IdentifierCapability::new);

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
    return Capabilities.IDENTIFIER_CAPABILITY.orEmpty(capability, identifierCapabilityLO);
  }

  @Override
  public Tag serializeNBT() {

    CompoundTag blockPosNBT = new CompoundTag();

    identifierCapabilityLO.ifPresent((instance) -> {
      if (instance.getBlockPos() != null) {
        blockPosNBT.putInt("x", instance.getBlockPos().getX());
        blockPosNBT.putInt("y", instance.getBlockPos().getY());
        blockPosNBT.putInt("z", instance.getBlockPos().getZ());
      }
    });

    return blockPosNBT;
  }

  @Override
  public void deserializeNBT(Tag nbt) {

    identifierCapabilityLO.ifPresent((instance) -> {
      CompoundTag blockPosNBT = (CompoundTag) nbt;
      if (blockPosNBT.contains("x") && blockPosNBT.contains("y") && blockPosNBT.contains("z"))
        instance.setBlockPos(new BlockPos(blockPosNBT.getInt("x"), blockPosNBT.getInt("y"), blockPosNBT.getInt("z")));
    });
  }

}
