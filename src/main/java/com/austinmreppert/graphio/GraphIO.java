package com.austinmreppert.graphio;

import com.austinmreppert.graphio.capabilities.Capabilities;
import com.austinmreppert.graphio.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main class for the mod.
 */
@Mod(GraphIO.MOD_ID)
public class GraphIO {

  public static final Logger LOGGER = LogManager.getLogger();
  public static final String MOD_ID = "graphio";

  public GraphIO() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);

    MinecraftForge.EVENT_BUS.register(this);
  }

  private void setup(final FMLCommonSetupEvent event) {
    PacketHandler.init();
  }

}
