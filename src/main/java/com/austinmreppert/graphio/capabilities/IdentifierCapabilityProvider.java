package com.austinmreppert.graphio.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class IdentifierCapabilityProvider implements ICapabilitySerializable<CompoundTag> {

  private final LazyOptional<IIdentifierCapability> identifierCapabilityLO = LazyOptional.of(IdentifierCapability::new);

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> capability, @Nullable final Direction side) {
    return Capabilities.IDENTIFIER_CAPABILITY.orEmpty(capability, identifierCapabilityLO);
  }

  @Override
  public CompoundTag serializeNBT() {
    final var nbt = new AtomicReference<>(new CompoundTag());

    identifierCapabilityLO.ifPresent((final var instance) -> {
      nbt.set(instance.serializeNBT());
    });

    return nbt.get();
  }

  @Override
  public void deserializeNBT(final CompoundTag nbt) {
    identifierCapabilityLO.ifPresent((final var instance) -> {
      instance.deserializeNBT(nbt);
    });
  }

}
