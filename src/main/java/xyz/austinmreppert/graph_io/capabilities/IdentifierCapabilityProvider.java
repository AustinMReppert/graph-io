package xyz.austinmreppert.graph_io.capabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
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
  public INBT serializeNBT() {
    return Capabilities.IDENTIFIER_CAPABILITY.getStorage().writeNBT(Capabilities.IDENTIFIER_CAPABILITY,
      identifierCapabilityLO.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty.")), null);
  }

  @Override
  public void deserializeNBT(INBT nbt) {
    Capabilities.IDENTIFIER_CAPABILITY.getStorage().readNBT(Capabilities.IDENTIFIER_CAPABILITY,
      identifierCapabilityLO.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty.")), null, nbt);
  }

}
