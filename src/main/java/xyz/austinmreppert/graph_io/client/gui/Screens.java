package xyz.austinmreppert.graph_io.client.gui;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.container.ContainerTypes;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Screens {

  @SubscribeEvent
  public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
      Screens.register();
    });
  }

  public static void register() {
    MenuScreens.register(ContainerTypes.ROUTER_CONTAINER, RouterScreen::new);
  }

}
