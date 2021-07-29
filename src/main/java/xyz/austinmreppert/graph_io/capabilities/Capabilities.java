package xyz.austinmreppert.graph_io.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class Capabilities {

  @CapabilityInject(IIdentifierCapability.class)
  public static Capability<IIdentifierCapability> IDENTIFIER_CAPABILITY = null;

  public static void register() {
    CapabilityManager.INSTANCE.register(IIdentifierCapability.class);
  }

}
