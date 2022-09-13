package com.austinmreppert.graphio.capabilities;

import com.austinmreppert.graphio.GraphIO;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Capabilities {

  public static final Capability<IIdentifierCapability> IDENTIFIER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

  @SubscribeEvent
  public void registerCaps(RegisterCapabilitiesEvent event) {
    event.register(IIdentifierCapability.class);
  }

}
